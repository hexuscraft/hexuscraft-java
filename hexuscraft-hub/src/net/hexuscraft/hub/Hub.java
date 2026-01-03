package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.hub.doublejump.MiniPluginDoubleJump;
import net.hexuscraft.hub.hubscoreboard.MiniPluginHubScoreboard;
import net.hexuscraft.hub.player.MiniPluginPlayer;
import net.hexuscraft.hub.team.MiniPluginTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

public final class Hub extends HexusPlugin {

    public Location _spawn = null;

    @Override
    public void load() {
        require(new MiniPluginDoubleJump(this));
        require(new MiniPluginPlayer(this));
        require(new MiniPluginTeam(this));
        require(new MiniPluginHubScoreboard(this));
    }

    @Override
    public void enable() {
        getServer().setDefaultGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        final World world = event.getWorld();
        logInfo("WORLD NAME: '" + world.getName() + "'");
        if (!world.getName().equals("world")) return;

        _spawn = new Location(world, 0, 100, 0, 0, 0);

        try {
            final Scanner scanner = new Scanner(Path.of(world.getWorldFolder().getPath(), "_spawn.dat").toFile());
            _spawn = new Location(event.getWorld(), scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble(),
                    scanner.nextFloat(), scanner.nextFloat());
        } catch (FileNotFoundException ex) {
            logInfo("Could not locate _spawn.dat in world '" + world.getName() + "'");
        }
    }

}