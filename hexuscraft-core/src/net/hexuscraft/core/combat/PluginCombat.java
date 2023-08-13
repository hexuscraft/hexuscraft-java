package net.hexuscraft.core.combat;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginCombat extends MiniPlugin {

    public PluginCombat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Combat");
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        _javaPlugin.getServer().getScheduler().scheduleSyncDelayedTask(_javaPlugin, () -> {
            Player player = event.getEntity();
            if (player.isDead()) {
                player.spigot().respawn();
            }
        }, 15);
    }

}