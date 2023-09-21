package net.hexuscraft.hub;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.entity.PluginEntity;
import net.hexuscraft.hub.player.PluginPlayer;
import net.hexuscraft.hub.team.PluginTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;

public class Hub extends HexusPlugin {

    @Override
    public void load() {
        register(new PluginPlayer(this));
        register(new PluginTeam(this));

        getServer().setDefaultGameMode(GameMode.ADVENTURE);
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

}