package net.hexuscraft.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.QueryResponse;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.proxy.database.PluginDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Plugin(id = "hexuscraft-proxy", name = "Proxy", version = "1.0.0")
public class Proxy {

    private final PluginDatabase _pluginDatabase;

    private final ProxyServer _server;
    private final Logger _logger;
    private final Path _dataDirectory;

    private final String MOTD_PREFIX = String.join("\n", new String[]{
            "         §9§m     §8§m[  §r  §6§lHexuscraft§r §f§lNetwork§r  §8§m  ]§9§m     §r",
            "§f§l ▶ §r"
    });

    private int _playerCount = 0;
    private int _maxPlayerCount = 0;

    private String _motd = MOTD_PREFIX;

    @Inject
    public Proxy(final ProxyServer server, final Logger logger, final @DataDirectory Path dataDirectory) {
        _server = server;
        _logger = logger;
        _dataDirectory = dataDirectory;

        _pluginDatabase = new PluginDatabase();
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        final CommandManager commandManager = _server.getCommandManager();
        commandManager.unregister("server");
        commandManager.unregister("velocity");

        _server.getScheduler()
                .buildTask(this, this::updateRegisteredServers)
                .repeat(Duration.ofSeconds(1))
                .delay(Duration.ofSeconds(0))
                .schedule();

        _server.getScheduler()
                .buildTask(this, this::updatePlayerCounts)
                .repeat(Duration.ofSeconds(1))
                .delay(Duration.ofSeconds(0))
                .schedule();
    }

    private void updateRegisteredServers() {
        JedisPooled jedis = _pluginDatabase.getJedisPooled();

        _motd = MOTD_PREFIX + ServerQueries.getMotd(jedis);

        final Set<ServerInfo> serverInfoSet = new HashSet<>();
        Arrays.stream(ServerQueries.getServers(jedis))
                .forEach(serverData -> serverInfoSet.add(new ServerInfo(serverData._name, new InetSocketAddress(serverData._address, serverData._port))));

        _server.getAllServers().stream().map(RegisteredServer::getServerInfo).forEach(_server::unregisterServer);
        serverInfoSet.forEach(_server::registerServer);
    }

    private void updatePlayerCounts() {
        //noinspection ReassignedVariable
        int playerCount = 0;
        //noinspection ReassignedVariable
        int maxPlayerCount = 0;

        for (RegisteredServer registeredServer : _server.getAllServers()) {
            try {
                final Optional<ServerPing.Players> optionalPlayers = registeredServer.ping().get().getPlayers();
                if (optionalPlayers.isEmpty()) continue;

                final ServerPing.Players players = optionalPlayers.get();
                playerCount += players.getOnline();
                maxPlayerCount += players.getMax();
            } catch (final InterruptedException | ExecutionException | RuntimeException ex) {
                _logger.info("Could not ping server '" + registeredServer.getServerInfo().getName() + "': " + ex.getMessage());
            }
        }

        _playerCount = playerCount;
        _maxPlayerCount = maxPlayerCount;
    }

    @Subscribe
    private void onProxyQuery(final ProxyQueryEvent event) {
        final QueryResponse.Builder builder = QueryResponse.builder();
        builder.players(
                "§r",
                "    §6§lHexuscraft§r §f§lNetwork§r    ",
                "§r",
                "  §f§l▶§r  Mini Games",
                "  §f§l▶§r  Private Servers",
                "  §f§l▶§r  Tournaments",
                "§r"
        );
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

        builder.description(Component.text(_motd));
        builder.samplePlayers(
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("    §6§lHexuscraft§r §f§lNetwork§r    ", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Mini Games", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Private Servers", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("  §f§l▶§r  Tournaments", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L))
        );
        builder.version(new ServerPing.Version(Math.max(ProtocolVersion.MINECRAFT_1_8.getProtocol(), event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
        try {
            builder.favicon(Favicon.create(Path.of("server-icon.png")));
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        event.setPing(builder.build());
    }

    @Subscribe
    private void onKickedFromServer(final KickedFromServerEvent event) {
        final Optional<Component> kickReason = event.getServerKickReason();
        if (kickReason.isEmpty()) {
            return; // let velocity handle this
        }
        event.getPlayer().disconnect(kickReason.get());
    }

    @Subscribe
    private void onPlayerChooseInitialServer(final PlayerChooseInitialServerEvent event) {
        if (event.getPlayer().getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
            event.setInitialServer(null);
            final TextComponent kickComponent = Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text("You must be playing Minecraft 1.8 or newer to play on "))
                    .append(Component.text("Hexuscraft", NamedTextColor.YELLOW))
                    .append(Component.text(".\n\n"))
                    .append(Component.text("www.hexuscraft.net", NamedTextColor.YELLOW))
                    .build();
            event.getPlayer().disconnect(kickComponent);
        }

        final RegisteredServer[] lobbyServers = _server.getAllServers().stream().filter(registeredServer -> registeredServer.getServerInfo().getName().split("-")[0].equals("Lobby")).toArray(RegisteredServer[]::new);
        if (lobbyServers.length == 0) {
            return; // let velocity handle this. (how are there no lobbies available??)
        }

        event.setInitialServer(lobbyServers[new Random().nextInt(lobbyServers.length)]);
    }

}
