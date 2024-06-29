package net.hexuscraft.core.teleport;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.teleport.command.CommandTeleport;

import java.util.Map;

public final class MiniPluginTeleport extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_TELEPORT,
        COMMAND_TELEPORT_COORDINATES,
        COMMAND_TELEPORT_OTHERS
    }

    private MiniPluginCommand _pluginCommand;

    public MiniPluginTeleport(final HexusPlugin plugin) {
        super(plugin, "Teleport");

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_TELEPORT);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_TELEPORT_COORDINATES);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_TELEPORT_OTHERS);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandTeleport(this));
    }

}
