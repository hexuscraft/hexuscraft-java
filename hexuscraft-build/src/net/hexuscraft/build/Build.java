package net.hexuscraft.build;

import net.hexuscraft.build.parse.MiniPluginParse;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.HexusPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Build extends HexusPlugin {

    @Override
    public void load() {
        new MiniPluginParse(this);
        new MiniPluginWorld(this);
    }

    public Location getSpawn() {
        return new Location(getServer().getWorld("world"), 0, 100, 0, 0, 0);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.CREATIVE);
    }

}