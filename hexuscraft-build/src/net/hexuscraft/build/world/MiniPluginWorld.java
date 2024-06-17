package net.hexuscraft.build.world;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.command.CommandSpawn;
import net.hexuscraft.build.world.command.CommandWorld;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public final class MiniPluginWorld extends MiniPlugin<Build> {

    public enum PERM implements IPermission {
        COMMAND_SPAWN,
        COMMAND_WORLD,
        COMMAND_WORLD_CREATE
    }

    private MiniPluginCommand _miniPluginCommand = null;

    public MiniPluginWorld(final Build plugin) {
        super(plugin, "World");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SPAWN);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_WORLD);
        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_WORLD_CREATE);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandSpawn(this));
        _miniPluginCommand.register(new CommandWorld(this));
    }

}
