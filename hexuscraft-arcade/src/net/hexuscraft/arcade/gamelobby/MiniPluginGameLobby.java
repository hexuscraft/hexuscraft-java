package net.hexuscraft.arcade.gamelobby;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.GameState;
import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.util.Map;

public final class MiniPluginGameLobby extends MiniPlugin<Arcade> {

    private MiniPluginGame _miniPluginGame;

    public MiniPluginGameLobby(final Arcade arcade) {
        super(arcade, "Game Lobby");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginGame = (MiniPluginGame) dependencies.get(MiniPluginGame.class);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (_miniPluginGame._gameState.equals(GameState.IN_PROGRESS)) return;

        final Player player = event.getPlayer();
        player.teleport(
                _hexusPlugin.getServer().getWorlds().getFirst().getSpawnLocation().add(new Vector(0.5, 0, 0.5)));
        player.resetPlayerTime();
        player.resetPlayerWeather();
        player.resetMaxHealth();
        //noinspection deprecation
        player.resetTitle();
        player.setSprinting(false);
        player.leaveVehicle();
        player.setHealth(player.getMaxHealth());
        player.setSaturation(0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setAllowFlight(true);
        player.setFlying(false);
        player.setExp(0);
        player.setCanPickupItems(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if (_miniPluginGame._gameState.equals(GameState.IN_PROGRESS)) return;
        if (!(event.getEntity() instanceof final Player player)) return;
        event.setCancelled(true);

        if (!event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) return;
        player.teleport(new Location(player.getWorld(), 0, 100, 0, 0, 0));
    }

}
