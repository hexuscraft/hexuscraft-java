package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.hub.entity.PluginEntity;
import net.hexuscraft.hub.player.PluginPlayer;
import net.hexuscraft.hub.team.PluginTeam;
import org.bukkit.Location;

public class Hub extends HexusPlugin {

    @Override
    public void load() {
        register(new PluginEntity(this));
        register(new PluginPlayer(this));
        register(new PluginTeam(this));
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

}