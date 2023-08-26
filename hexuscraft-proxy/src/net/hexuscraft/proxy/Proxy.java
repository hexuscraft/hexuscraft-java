package net.hexuscraft.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.*;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.QueryResponse;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.hexuscraft.database.serverdata.ServerGroupType;
import net.hexuscraft.proxy.database.PluginDatabase;
import net.kyori.adventure.text.Component;
import redis.clients.jedis.params.XReadGroupParams;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id = "hexuscraft-proxy", name = "Proxy", version = "1.0.0")
public class Proxy {

    final PluginDatabase _pluginDatabase;

    final ProxyServer _server;
    final Logger _logger;
    final Path _dataDirectory;

    final String MOTD_PREFIX = String.join("\n", new String[]{
            "§5§m--------§r§8§m]§r§d§m--§r  §6§lHexuscraft§r §e§lNetwork§r  §d§m--§r§8§m[§r§5§m--------§r",
            "§8§l>§r "
    });

    String _motd = MOTD_PREFIX;

    @Inject
    public Proxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        _server = server;
        _logger = logger;
        _dataDirectory = dataDirectory;

        _pluginDatabase = new PluginDatabase();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        _server.getCommandManager().unregister("server");

        _server.getScheduler().buildTask(this, () -> {
            _motd = MOTD_PREFIX + _pluginDatabase.getJedisPooled().get(ServerQueries.SERVERS_MOTD());

            Map<String, InetSocketAddress> serverInfoMap = new HashMap<>();
            _pluginDatabase.getJedisPooled().smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).forEach(uuid -> {
                ServerData serverData = new ServerData(_pluginDatabase.getJedisPooled().hgetAll(ServerQueries.SERVER(uuid)));
                ServerGroupData groupData = new ServerGroupData(_pluginDatabase.getJedisPooled().hgetAll(ServerQueries.SERVERGROUP(serverData._group)));
                if (groupData._type != ServerGroupType.DEDICATED) {
                    return;
                }
                serverInfoMap.put(serverData._name, new InetSocketAddress(serverData._serverIp, serverData._serverPort));
            });

            _server.getAllServers().forEach(registeredServer -> {
                _server.unregisterServer(registeredServer.getServerInfo());
            });
            serverInfoMap.forEach((name, address) -> {
                _server.registerServer(new ServerInfo(name, address));
            });
        }).repeat(Duration.ofSeconds(1)).delay(Duration.ofSeconds(0)).schedule();
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Optional<Component> kickReason = event.getServerKickReason();
        if (kickReason.isEmpty()) {
            return; // let velocity handle this
        }
        event.getPlayer().disconnect(kickReason.get());
    }

    @Subscribe
    public void onProxyQuery(ProxyQueryEvent event) {
        QueryResponse.Builder builder = QueryResponse.builder();
        int playerCount = _server.getPlayerCount();
        builder.currentPlayers(playerCount);
        builder.maxPlayers(playerCount + 1);
        event.setResponse(builder.build());
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder builder = ServerPing.builder();
        int playerCount = _server.getPlayerCount();
        builder.onlinePlayers(playerCount);
        builder.maximumPlayers(playerCount + 1);
        builder.description(Component.text(_motd));
        builder.version(new ServerPing.Version(Math.max(47, event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
        try {
            builder.favicon(Favicon.create(Path.of("server-icon.png")));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        event.setPing(builder.build());
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        RegisteredServer[] lobbyServers = _server.getAllServers().stream().filter(registeredServer -> registeredServer.getServerInfo().getName().split("-")[0].equals("Lobby")).toArray(RegisteredServer[]::new);
        if (lobbyServers.length == 0) {
            return; // let velocity handle this. how are there even no lobbies available??!
        }

        event.setInitialServer(lobbyServers[new Random().nextInt(lobbyServers.length)]);
    }

}
