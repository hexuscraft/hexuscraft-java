package net.hexuscraft.web;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.web.sales.WebSales;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Web extends HexusPlugin
{

    public Web()
    {
        super();

        require(new WebSales(this));
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        event.setJoinMessage(F.fSub("Join", player.getName()));
        player.teleport(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(F.fSub("Quit", event.getPlayer().getName()));
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
        {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event)
    {
        event.setTo(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
    }

}
