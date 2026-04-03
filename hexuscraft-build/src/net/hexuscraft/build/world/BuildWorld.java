package net.hexuscraft.build.world;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.command.CommandSpawn;
import net.hexuscraft.build.world.command.CommandWorld;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;

import java.util.Map;

public class BuildWorld extends MiniPlugin<Build>
{

    public enum PERM implements IPermission
    {
        COMMAND_SPAWN,
        COMMAND_WORLD,
        COMMAND_WORLD_CREATE
    }

    CoreCommand _coreCommand = null;

    public BuildWorld(Build plugin)
    {
        super(plugin, "World");

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_SPAWN);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_WORLD);
        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_WORLD_CREATE);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandSpawn(this));
        _coreCommand.register(new CommandWorld(this));
    }

}
