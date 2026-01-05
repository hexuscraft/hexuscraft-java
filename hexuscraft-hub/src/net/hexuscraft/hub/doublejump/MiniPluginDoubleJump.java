package net.hexuscraft.hub.doublejump;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.hub.Hub;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class MiniPluginDoubleJump extends MiniPlugin<Hub> {

    public MiniPluginDoubleJump(final Hub hub) {
        super(hub, "Double Jump");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.getPlayer().setAllowFlight(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChanged(final PlayerGameModeChangeEvent event) {
        // For some reason we need to delay this by a tick even though EventPriority.MONITOR should be the last to fire.
        // Probably some quirky nms logic when changing game mode.
        _hexusPlugin.runSyncLater(() -> event.getPlayer().setAllowFlight(true), 1);
    }

    @EventHandler
    public void onFlight(final PlayerToggleFlightEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (!event.isFlying()) return;

        event.setCancelled(true);
        player.setVelocity(player.getLocation().getDirection().setY(1));
        player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, Float.MAX_VALUE, 1);
    }

}
