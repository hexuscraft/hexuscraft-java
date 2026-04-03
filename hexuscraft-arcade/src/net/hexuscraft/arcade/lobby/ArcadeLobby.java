package net.hexuscraft.arcade.lobby;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.arcade.manager.GameState;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.util.Vector;

import java.util.Map;

public class ArcadeLobby extends MiniPlugin<Arcade>
{

    ArcadeManager _arcadeManager;

    public ArcadeLobby(Arcade arcade)
    {
        super(arcade, "Game Lobby");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _arcadeManager = (ArcadeManager) dependencies.get(ArcadeManager.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (_arcadeManager.getGameState() == GameState.IN_PROGRESS)
        {
            return;
        }

        Player player = event.getPlayer();
        player.teleport(_hexusPlugin.getServer()
                .getWorlds()
                .getFirst()
                .getSpawnLocation()
                .add(new Vector(0.5, 0, 0.5)));
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player player))
        {
            return;
        }
        if (!player.getWorld().equals(_hexusPlugin.getServer().getWorlds().getFirst()))
        {
            return;
        }

        if (event.getDamager() instanceof Player damager && damager.getGameMode().equals(GameMode.CREATIVE))
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player player))
        {
            return;
        }
        if (!player.getWorld().equals(_hexusPlugin.getServer().getWorlds().getFirst()))
        {
            return;
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))
        {
            return;
        }

        event.setCancelled(true);

        if (!event.getCause().equals(EntityDamageEvent.DamageCause.VOID))
        {
            return;
        }

        player.teleport(new Location(player.getWorld(), 0, 100, 0, 0, 0));
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event)
    {
        if (!event.getWorld().equals(_hexusPlugin.getServer().getWorlds().getFirst()))
        {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!event.getEntity().getWorld().equals(_hexusPlugin.getServer().getWorlds().getFirst()))
        {
            return;
        }
        event.setCancelled(true);
    }

}
