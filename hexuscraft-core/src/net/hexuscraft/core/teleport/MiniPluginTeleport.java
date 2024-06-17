package net.hexuscraft.core.teleport;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.teleport.command.CommandTeleport;

import java.util.Map;

public final class MiniPluginTeleport extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_TELEPORT,
        COMMAND_TELEPORT_COORDINATES,
        COMMAND_TELEPORT_OTHERS,
        COMMAND_TELEPORT_MULTIPLE
    }

    MiniPluginCommand _pluginCommand;

    public MiniPluginTeleport(final HexusPlugin plugin) {
        super(plugin, "Teleport");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandTeleport(this));
    }

}
