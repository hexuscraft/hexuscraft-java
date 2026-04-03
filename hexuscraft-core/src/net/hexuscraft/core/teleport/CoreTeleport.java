package net.hexuscraft.core.teleport;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.teleport.command.CommandTeleport;

import java.util.Map;

public class CoreTeleport extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_TELEPORT,
        COMMAND_TELEPORT_COORDINATES,
        COMMAND_TELEPORT_OTHERS
    }

    CoreCommand _pluginCommand;

    public CoreTeleport(HexusPlugin plugin)
    {
        super(plugin, "Teleport");

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_TELEPORT);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_TELEPORT_COORDINATES);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_TELEPORT_OTHERS);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable()
    {
        _pluginCommand.register(new CommandTeleport(this));
    }

}
