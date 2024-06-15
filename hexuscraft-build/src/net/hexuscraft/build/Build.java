package net.hexuscraft.build;

import net.hexuscraft.build.parse.MiniPluginParse;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.HexusPlugin;
import org.bukkit.Location;

public class Build extends HexusPlugin {

    @Override
    public void load() {
        new MiniPluginParse(this);
        new MiniPluginWorld(this);
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

}