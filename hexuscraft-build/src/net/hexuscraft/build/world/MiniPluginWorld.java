package net.hexuscraft.build.world;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.command.CommandSpawn;
import net.hexuscraft.build.world.command.CommandWorld;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;

import java.util.Map;

public final class MiniPluginWorld extends MiniPlugin<Build> {

    public enum PERM implements IPermission {
        COMMAND_SPAWN,
        COMMAND_WORLD,
        COMMAND_WORLD_CREATE
    }

    private PluginCommand _miniPluginCommand = null;

    public MiniPluginWorld(final Build plugin) {
        super(plugin, "World");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandSpawn(this));
        _miniPluginCommand.register(new CommandWorld(this));
    }

}
