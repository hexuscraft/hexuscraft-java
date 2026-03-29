package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.hub.doublejump.HubDoubleJump;
import net.hexuscraft.hub.hubscoreboard.HubScoreboard;
import net.hexuscraft.hub.news.HubNews;
import net.hexuscraft.hub.player.HubPlayer;
import net.hexuscraft.hub.team.HubTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

public class Hub extends HexusPlugin
{

    public Location _spawn = null;

    public Hub()
    {
        super();

        require(new HubDoubleJump(this));
        require(new HubScoreboard(this));
        require(new HubNews(this));
        require(new HubPlayer(this));
        require(new HubTeam(this));
    }

    @Override
    public void enable()
    {
        getServer().setDefaultGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event)
    {
        World world = event.getWorld();
        logInfo("WORLD NAME: '" + world.getName() + "'");
        if (!world.getName().equals("world"))
        {
            return;
        }

        _spawn = new Location(world, 0, 100, 0, 0, 0);

        try
        {
            Scanner scanner = new Scanner(Path.of(world.getWorldFolder().getPath(), "_spawn.dat").toFile());
            _spawn = new Location(event.getWorld(),
                                  scanner.nextDouble(),
                                  scanner.nextDouble(),
                                  scanner.nextDouble(),
                                  scanner.nextFloat(),
                                  scanner.nextFloat());
        }
        catch (FileNotFoundException ex)
        {
            logInfo("Could not locate _spawn.dat in world '" + world.getName() + "'");
        }
    }

}