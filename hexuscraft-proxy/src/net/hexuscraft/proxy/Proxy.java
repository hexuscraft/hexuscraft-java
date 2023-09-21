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

    private String _motd = MOTD_PREFIX;

    @Inject
    public Proxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        _server = server;
        _logger = logger;
        _dataDirectory = dataDirectory;

        _pluginDatabase = new PluginDatabase();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        CommandManager commandManager = _server.getCommandManager();
        commandManager.unregister("server");
        commandManager.unregister("velocity");

        _server.getScheduler().buildTask(this, () -> {
            JedisPooled jedis = _pluginDatabase.getJedisPooled();

            _motd = MOTD_PREFIX + ServerQueries.getMotd(jedis);

            final Map<String, InetSocketAddress> serverInfoMap = new HashMap<>();
            Arrays.stream(ServerQueries.getServers(jedis)).forEach(serverData -> {
                serverInfoMap.put(serverData._name, new InetSocketAddress(serverData._address, serverData._port));
            });

            _server.getAllServers().forEach(registeredServer -> _server.unregisterServer(registeredServer.getServerInfo()));
            serverInfoMap.forEach((name, address) -> _server.registerServer(new ServerInfo(name, address)));
        }).repeat(Duration.ofSeconds(1)).delay(Duration.ofSeconds(0)).schedule();
    }

    @Subscribe
    private void onProxyQuery(ProxyQueryEvent event) {
        QueryResponse.Builder builder = QueryResponse.builder();
        int playerCount = _server.getPlayerCount();
        builder.currentPlayers(playerCount);
        builder.maxPlayers(playerCount + 1);
        event.setResponse(builder.build());
    }

    @Subscribe
    private void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder builder = ServerPing.builder();
        int playerCount = _server.getPlayerCount();
        builder.onlinePlayers(playerCount);
        builder.maximumPlayers(playerCount + 1);
        builder.description(Component.text(_motd));
        builder.samplePlayers(
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("    §6§lHexuscraft§r §f§lNetwork§r    ", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§f§l ▶ §rMini Games", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§f§l ▶ §rPlayer Servers", new UUID(0L, 0L)),
                new ServerPing.SamplePlayer("§r", new UUID(0L, 0L))
        );
        builder.version(new ServerPing.Version(Math.max(ProtocolVersion.MINECRAFT_1_8.getProtocol(), event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
        try {
            builder.favicon(Favicon.create(Path.of("server-icon.png")));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        event.setPing(builder.build());
    }

    @Subscribe
    private void onKickedFromServer(KickedFromServerEvent event) {
        Optional<Component> kickReason = event.getServerKickReason();
        if (kickReason.isEmpty()) {
            return; // let velocity handle this
        }
        event.getPlayer().disconnect(kickReason.get());
    }

    @Subscribe
    private void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        if (event.getPlayer().getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
            event.setInitialServer(null);
            final TextComponent kickComponent = Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text("You must be using Minecraft 1.8 or newer to play on "))
                    .append(Component.text("Hexuscraft", NamedTextColor.YELLOW))
                    .append(Component.text("!"))
                    .build();
            event.getPlayer().disconnect(kickComponent);
        }

        RegisteredServer[] lobbyServers = _server.getAllServers().stream().filter(registeredServer -> registeredServer.getServerInfo().getName().split("-")[0].equals("Lobby")).toArray(RegisteredServer[]::new);
        if (lobbyServers.length == 0) {
            return; // let velocity handle this. how are there even no lobbies available??!
        }

        event.setInitialServer(lobbyServers[new Random().nextInt(lobbyServers.length)]);
    }

}
