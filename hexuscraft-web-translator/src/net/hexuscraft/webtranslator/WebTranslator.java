package net.hexuscraft.webtranslator;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.buildversion.CoreBuildVersion;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WebTranslator extends HexusPlugin
{

    public WebTranslator()
    {
        super(false);

        require(new CoreBuildVersion(this));
        require(new CoreCommand(this));
        require(new CoreDatabase(this));
        require(new CorePermission(this));
        require(new CorePortal(this));
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        event.setJoinMessage(F.fSub("Join", player.getName()));
        player.teleport(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(F.fSub("Quit", event.getPlayer().getName()));
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
        {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event)
    {
        event.setTo(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
    }

}
