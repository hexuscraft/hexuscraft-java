package net.hexuscraft.build;

import net.hexuscraft.build.parse.BuildParse;
import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.teleport.CoreTeleport;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class Build extends HexusPlugin
{

    public Build()
    {
        super();

        PermissionGroup.BUILD_TEAM._permissions.add(CoreTeleport.PERM.COMMAND_TELEPORT);
        PermissionGroup.BUILD_TEAM._permissions.add(CoreTeleport.PERM.COMMAND_TELEPORT_COORDINATES);
        PermissionGroup.BUILD_LEAD._permissions.add(CoreTeleport.PERM.COMMAND_TELEPORT_OTHERS);

        require(new BuildParse(this));
        require(new BuildWorld(this));
    }

    public Location getSpawn()
    {
        return getServer().getWorlds().getFirst().getSpawnLocation();
    }

    @EventHandler
    void onWorldLoad(WorldLoadEvent event)
    {
        event.getWorld().setSpawnLocation(0, 100, 0);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        event.getPlayer().teleport(getSpawn());
        event.getPlayer().setGameMode(GameMode.CREATIVE);
    }

}