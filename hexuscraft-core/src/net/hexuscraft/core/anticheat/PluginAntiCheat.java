package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PluginAntiCheat extends MiniPlugin {

    Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies;

    Map<Player, CheatClient> cheatClientMap;

    public PluginAntiCheat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Anti Cheat");
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        this.dependencies = dependencies;
        cheatClientMap = new HashMap<>();
    }

    @Override
    public final void onEnable() {
        for (Player player : _javaPlugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Override
    public final void onDisable() {
        for (CheatClient client : cheatClientMap.values()) {
            client.disable();
        }
        cheatClientMap.clear();
        cheatClientMap = null;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CheatClient client = new CheatClient(this, player);
        cheatClientMap.put(player, client);
        client.load(dependencies);
        client.enable();
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cheatClientMap.get(player).disable();
        cheatClientMap.remove(player);
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) { return; }
        double distance = player.getLocation().distance(event.getRightClicked().getLocation());

        if (distance > 4) { event.setCancelled(true); }

        if (distance > 5.5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.HIGH);
        } else if (distance > 5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.MEDIUM);
        } else if (distance > 4.5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.LOW);
        }
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) { return; }
        Player player = ((Player) event.getDamager()).getPlayer();
        double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());

        if (distance > 4) { event.setCancelled(true); }

        if (player.getGameMode() == GameMode.CREATIVE) { return; }

        if (distance > 5.5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.HIGH);
        } else if (distance > 5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.MEDIUM);
        } else if (distance > 4.5) {
            cheatClientMap.get(player).flag(player, "Reach", CheatSeverity.LOW);
        }
    }

}