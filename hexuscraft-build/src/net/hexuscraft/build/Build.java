package net.hexuscraft.build;

import net.hexuscraft.core.HexusPlugin;
import org.bukkit.Location;

public class Build extends HexusPlugin {

    @Override
    public void load() {
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

}