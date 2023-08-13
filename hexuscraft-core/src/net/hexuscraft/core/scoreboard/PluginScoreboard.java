package net.hexuscraft.core.scoreboard;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PluginScoreboard extends MiniPlugin {

    Map<Class<? extends MiniPlugin>, MiniPlugin> miniPluginClassMap;

    Map<Player, ClientScoreboard> scoreboardClientMap;

    public PluginScoreboard(JavaPlugin javaPlugin) {
        super(javaPlugin, "Scoreboard");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> miniPluginClassMap) {
        this.miniPluginClassMap = miniPluginClassMap;
        scoreboardClientMap = new HashMap<>();
    }

    @Override
    public void onEnable() {
        for (Player player : _javaPlugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Override
    public void onDisable() {
        for (Player player : _javaPlugin.getServer().getOnlinePlayers()) {
            onPlayerQuit(new PlayerQuitEvent(player, null));
        }
        scoreboardClientMap.clear();
        scoreboardClientMap = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ClientScoreboard client = new ClientScoreboard(this, player);
        scoreboardClientMap.put(player, client);
        client.load(miniPluginClassMap);
        client.enable();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        scoreboardClientMap.get(player).disable();
        scoreboardClientMap.remove(player);
    }

}
