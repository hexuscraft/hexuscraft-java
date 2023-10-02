package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.hub.player.PluginPlayer;
import net.hexuscraft.hub.team.PluginTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

public class Hub extends HexusPlugin {

    @Override
    public void load() {
        require(new PluginPlayer(this));
        require(new PluginTeam(this));

        getServer().setDefaultGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void on(WorldLoadEvent event) {
        getServer().setDefaultGameMode(GameMode.ADVENTURE);
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

}