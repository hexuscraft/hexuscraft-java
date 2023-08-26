package net.hexuscraft.core.combat;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PluginCombat extends MiniPlugin {

    Map<Player, BukkitRunnable> _pendingRespawns;

    public PluginCombat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Combat");

        _pendingRespawns = new HashMap<>();
    }

    @Override
    public void onDisable() {
        _pendingRespawns.forEach((player, runnable) -> runnable.cancel());
        _pendingRespawns.clear();
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isDead()) { return; }
                player.spigot().respawn();
            }
        };
        runnable.runTaskLater(_javaPlugin, 15);

        _pendingRespawns.put(player, runnable);
    }

}