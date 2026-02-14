package net.hexuscraft.build;

import net.hexuscraft.build.parse.MiniPluginParse;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.teleport.MiniPluginTeleport;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

public final class Build extends HexusPlugin {

    public Build() {
        super();

        PermissionGroup.BUILD_TEAM._permissions.add(MiniPluginTeleport.PERM.COMMAND_TELEPORT);
        PermissionGroup.BUILD_TEAM._permissions.add(MiniPluginTeleport.PERM.COMMAND_TELEPORT_COORDINATES);
        PermissionGroup.BUILD_LEAD._permissions.add(MiniPluginTeleport.PERM.COMMAND_TELEPORT_OTHERS);

        require(MiniPluginParse.class);
        require(MiniPluginWorld.class);
    }

    public Location getSpawn() {
        return getServer().getWorlds()
                .getFirst()
                .getSpawnLocation();
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        event.getWorld()
                .setSpawnLocation(0,
                        100,
                        0);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        event.getPlayer()
                .teleport(getSpawn());
        event.getPlayer()
                .setGameMode(GameMode.CREATIVE);
    }

}