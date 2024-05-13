package net.hexuscraft.hub.doublejump;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.hub.Hub;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PluginDoubleJump extends MiniPlugin<Hub> {

    public PluginDoubleJump(final Hub hub) {
        super(hub, "Double Jump");
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if (!((Entity) player).isOnGround()) return;
        player.setAllowFlight(true);
    }

    @EventHandler
    public void onFlight(final PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (!event.isFlying()) return;

        event.setCancelled(true);

        final Player player = event.getPlayer();

        player.setFlying(false);
        player.setAllowFlight(false);

        player.setVelocity(player.getLocation().getDirection().setY(1));
        player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1000000, 1);
    }

}
