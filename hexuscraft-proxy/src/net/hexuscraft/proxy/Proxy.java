package net.hexuscraft.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.QueryResponse;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.data.PunishData;
import net.hexuscraft.common.database.queries.PunishQueries;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PunishType;
import net.hexuscraft.proxy.database.PluginDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id = "hexuscraft-proxy", name = "Proxy", version = "1.0.0")
public final class Proxy {

    private final String MOTD_PREFIX =
            String.join("\n", "            §6§lHexuscraft§r §f§lNetwork§r  §9[1.8-1.21]§r", " §f§l▶§r ");

    private final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final PluginDatabase _pluginDatabase;
    private final ProxyServer _server;
    private final Logger _logger;
    private final AtomicReference<String> _motd;
    private int _playerCount;
    private int _maxPlayerCount;

    @Inject
    public Proxy(final ProxyServer server, final Logger logger) {
        _pluginDatabase = new PluginDatabase();
        _server = server;
        _logger = logger;
        _motd = new AtomicReference<>(MOTD_PREFIX + "<insert funny message here>");
        _playerCount = 0;
        _maxPlayerCount = 0;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        final CommandManager commandManager = _server.getCommandManager();
        commandManager.getAliases().forEach(commandManager::unregister);

        _server.getScheduler().buildTask(this, this::updateMOTD).repeat(Duration.ofSeconds(5))
                .delay(Duration.ofSeconds(0)).schedule();
        _server.getScheduler().buildTask(this, this::updateRegisteredServers).repeat(Duration.ofSeconds(1))
                .delay(Duration.ofSeconds(0)).schedule();
        _server.getScheduler().buildTask(this, this::updatePlayerCounts).repeat(Duration.ofSeconds(1))
                .delay(Duration.ofSeconds(0)).schedule();
    }

    private void updateMOTD() {
        final UnifiedJedis jedis = _pluginDatabase.getUnifiedJedis();
        _motd.set(MOTD_PREFIX + ServerQueries.getMotd(jedis));

    }

    private void updateRegisteredServers() {
        final UnifiedJedis jedis = _pluginDatabase.getUnifiedJedis();

        final List<ServerInfo> allServers = new ArrayList<>();
        final List<String> fallbackServers = new ArrayList<>();

        Arrays.stream(ServerQueries.getServers(jedis)).forEach(serverData -> {
            if (serverData._updatedByMonitor) return;

            final ServerInfo serverInfo =
                    new ServerInfo(serverData._name, new InetSocketAddress(serverData._address, serverData._port));
            allServers.add(serverInfo);

            if (serverData._group.equals("Lobby")) fallbackServers.add(serverData._name);
        });

        _server.getConfiguration().getAttemptConnectionOrder().clear();
        _server.getAllServers().stream().map(RegisteredServer::getServerInfo).forEach(_server::unregisterServer);

        allServers.forEach(_server::registerServer);

        final List<String> fallbackServerConfig = _server.getConfiguration().getAttemptConnectionOrder();
        fallbackServerConfig.addAll(fallbackServers);
        fallbackServerConfig.sort(String::compareTo);
    }

    private void updatePlayerCounts() {
        final AtomicInteger playerCount = new AtomicInteger(0);
        final AtomicInteger maxPlayerCount = new AtomicInteger(0);

        _server.getAllServers().forEach(registeredServer -> {
            try {
                final Optional<ServerPing.Players> optionalPlayers = registeredServer.ping().get().getPlayers();
                if (optionalPlayers.isEmpty()) return;

                final ServerPing.Players players = optionalPlayers.get();
                playerCount.addAndGet(players.getOnline());
                maxPlayerCount.addAndGet(players.getMax());
            } catch (final InterruptedException | ExecutionException | RuntimeException ex) {
                _logger.info("Could not ping server '" + registeredServer.getServerInfo().getName() + "': " +
                        ex.getMessage());
            }
        });

        _playerCount = playerCount.get();
        _maxPlayerCount = maxPlayerCount.get();
    }

    @Subscribe
    private void onProxyQuery(final ProxyQueryEvent event) {
        final QueryResponse.Builder builder = QueryResponse.builder();
        builder.players("§r", "    §6§lHexuscraft§r §f§lNetwork§r    ", "§r", "  §f§l▶§r  Mini Games",
                "  §f§l▶§r  Private Servers", "  §f§l▶§r  Tournaments", "§r");
        builder.clearPlugins();
        builder.proxyVersion("Minecraft 1.8");
        builder.gameVersion("Minecraft 1.8");
        builder.currentPlayers(_playerCount);
        builder.maxPlayers(_maxPlayerCount);
        event.setResponse(builder.build());
    }

    @Subscribe
    private void onProxyPing(final ProxyPingEvent event) {
        final ServerPing.Builder builder = ServerPing.builder();

        builder.onlinePlayers(_playerCount);
        builder.maximumPlayers(_maxPlayerCount);

        builder.description(Component.text(_motd.get()));
        builder.samplePlayers(new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("    §6§lHexuscraft§r §f§lNetwork§r    ", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Mini Games", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Private Servers", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Tournaments", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)));
        builder.version(new ServerPing.Version(Math.max(ProtocolVersion.MINECRAFT_1_8.getProtocol(),
                event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
        try {
            builder.favicon(Favicon.create(Path.of("server-icon.png")));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        event.setPing(builder.build());
    }

    @Subscribe
    private void onLogin(final LoginEvent event) {
        try {
            final UnifiedJedis jedis = _pluginDatabase.getUnifiedJedis();
            final Set<UUID> punishmentIds =
                    jedis.smembers(PunishQueries.LIST(event.getPlayer().getUniqueId())).stream().map(UUID::fromString)
                            .collect(Collectors.toSet());

            // We want to display the longest ban remaining.
            // If there are multiple bans with the same remaining time (usually multiple perm bans), display the most recent ban.
            // If there are multiple bans matching this and were also applied at the EXACT same time (??), fate decides the displayed message.

            final Set<PunishData> activePunishments = new HashSet<>();

            for (final UUID punishmentUniqueId : punishmentIds) {
                try {
                    final Map<String, String> rawData =
                            new HashMap<>(jedis.hgetAll(PunishQueries.PUNISHMENT(punishmentUniqueId)));
                    rawData.put("id", punishmentUniqueId.toString());

                    final PunishData punishData = new PunishData(rawData);
                    if (!punishData.active) continue;
                    if (!punishData.type.equals(PunishType.BAN)) continue;

                    if (punishData.length == -1) { // permanent ban
                        activePunishments.add(punishData);
                        continue;
                    }

                    final long remaining = punishData.getRemaining();
                    if (remaining <= 0) {
                        _pluginDatabase.getUnifiedJedis().hset(PunishQueries.PUNISHMENT(punishmentUniqueId),
                                Map.of("active", "false", "removeOrigin", Long.toString(System.currentTimeMillis()),
                                        "removeReason", "EXPIRED", "removeServer",
                                        "Proxy-" + _server.getBoundAddress().toString(), "removeStaffId",
                                        DEFAULT_UUID.toString(), "removeStaffServer",
                                        "Proxy-" + _server.getBoundAddress().toString()));
                        continue;
                    }

                    activePunishments.add(punishData);
                } catch (final JedisException ex) {
                    _logger.warning("Error while checking punish data for '" + event.getPlayer().getUsername() + "': " +
                            ex.getMessage());
                }
            }

            if (activePunishments.isEmpty()) return;

            final AtomicReference<PunishData> punishData = new AtomicReference<>();
            if (activePunishments.size() > 1) {
                for (PunishData data : activePunishments) {
                    if (punishData.get() == null) {
                        punishData.set(data);
                        continue;
                    }
                    punishData.set(punishData.get().compare(data));
                }
            } else {
                punishData.set(activePunishments.iterator().next());
            }

            event.setResult(ResultedEvent.ComponentResult.denied(Component.text(F.fPunish(punishData.get()))));
        } catch (final JedisException ex) {
            _logger.warning("Error while fetching punishment data for '" + event.getPlayer().getUsername() + "': " +
                    ex.getMessage());
        }

    }

    @Subscribe
    private void onPlayerChooseInitialServer(final PlayerChooseInitialServerEvent event) {
        final Player player = event.getPlayer();

        if (player.getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
            event.setInitialServer(null);
            player.disconnect(Component.text().color(NamedTextColor.RED)
                    .append(Component.text("Your game client is too outdated."))
                    .append(Component.text("\nPlease use Minecraft 1.8 or newer to join Hexuscraft.",
                            NamedTextColor.GRAY))
                    .append(Component.text("\n\nwww.hexuscraft.net", NamedTextColor.YELLOW)).build());
            return;
        }

        final RegisteredServer[] lobbyServers = _server.getAllServers().stream()
                .filter(registeredServer -> registeredServer.getServerInfo().getName().split("-(?=[^-]*$)")[0].equals(
                        "Lobby")).toArray(RegisteredServer[]::new);
        if (lobbyServers.length == 0) {
            player.disconnect(Component.text().color(NamedTextColor.RED)
                    .append(Component.text("There are currently no lobby servers available."))
                    .append(Component.text("\nPlease try again later.", NamedTextColor.GRAY))
                    .append(Component.text("\n\nwww.hexuscraft.net", NamedTextColor.YELLOW)).build());
            return;
        }

        event.setInitialServer(lobbyServers[new Random().nextInt(lobbyServers.length)]);
    }

}
