package net.hexuscraft.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.QueryResponse;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

@Plugin(id = "hexuscraft-proxy", name = "Proxy", version = "1.0.0")
public class Proxy {

    final ProxyServer server;
    final Logger logger;
    final Path dataDirectory;

    String pingDescription = "§5§m--------§r§8§m]§r§d§m--§r  §6§lHexuscraft§r §e§lNetwork§r  §d§m--§r§8§m[§r§5§m--------§r"
            + "\n§8§l>§r §aTower Battles§r  §8§l§mo§r  §cSurvival Games§r  §8§l§mo§r  §bSkywars§r";

    @Inject
    public Proxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        server.getCommandManager().unregister("server");
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Optional<Component> kickReason = event.getServerKickReason();
        if (kickReason.isEmpty()) {
            return;
        } // let velocity handle this
        event.getPlayer().disconnect(kickReason.get());
    }

    @Subscribe
    public void onProxyQuery(ProxyQueryEvent event) {
        QueryResponse.Builder builder = QueryResponse.builder();
        int playerCount = server.getPlayerCount();
        builder.currentPlayers(playerCount);
        builder.maxPlayers(playerCount + 1);
        event.setResponse(builder.build());
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing.Builder builder = ServerPing.builder();
        int playerCount = server.getPlayerCount();
        builder.onlinePlayers(playerCount);
        builder.maximumPlayers(playerCount + 1);
        builder.description(Component.text(pingDescription));
        builder.version(new ServerPing.Version(Math.max(47, event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
        try {
            builder.favicon(Favicon.create(Path.of("server-icon.png")));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        event.setPing(builder.build());
    }

}
