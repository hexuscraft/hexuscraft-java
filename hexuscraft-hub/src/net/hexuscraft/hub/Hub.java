package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.hub.doublejump.PluginDoubleJump;
import net.hexuscraft.hub.player.PluginPlayer;
import net.hexuscraft.hub.team.PluginTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

public class Hub extends HexusPlugin {

    public Location _spawn = new Location(null, 0, 100, 0, 0, 0);

    @Override
    public void load() {
        require(new PluginDoubleJump(this));
        require(new PluginPlayer(this));
        require(new PluginTeam(this));
    }

    @EventHandler
    public final void onWorldLoad(final WorldLoadEvent event) {
        final World world = event.getWorld();
        if (!world.getName().equals("world")) return;

        getServer().setDefaultGameMode(GameMode.ADVENTURE);
        _spawn.setWorld(world);

        try {
            final Scanner scanner = new Scanner(Path.of(world.getWorldFolder().getPath(), "_spawn.dat").toFile());
            _spawn.setX(scanner.nextDouble());
            _spawn.setY(scanner.nextDouble());
            _spawn.setZ(scanner.nextDouble());
            _spawn.setYaw(scanner.nextFloat());
            _spawn.setPitch(scanner.nextFloat());
        } catch (FileNotFoundException ex) {
            log("Could not locate _spawn.dat in world '" + world.getName() + "'");
        }
    }

}