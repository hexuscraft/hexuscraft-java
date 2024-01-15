package net.hexuscraft.hub.doublejump;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.entity.EntityMoveEvent;
import net.hexuscraft.hub.Hub;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class PluginDoubleJump extends MiniPlugin {

    PluginDoubleJump(final Hub hub) {
        super(hub, "Double Jump");
    }

    @EventHandler
    public void onMove(final EntityMoveEvent event) {
        if (!(event._entity instanceof final Player player)) return;
        if (!event._entity.isOnGround()) return;
        player.setAllowFlight(true);
    }

    @EventHandler
    public void onFlight(final PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (!event.isFlying()) return;

        event.setCancelled(true);

        final Player player = event.getPlayer();
        final Location location = player.getLocation();

        player.setFlying(false);
        player.setAllowFlight(false);

        player.setVelocity(location.toVector().add(new Vector(0, 1, 3)));
        player.playSound(location, Sound.GHAST_FIREBALL, 1000000, 1);
    }

}
