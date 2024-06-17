package net.hexuscraft.core.item;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.item.command.CommandClear;
import net.hexuscraft.core.item.command.CommandGive;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public final class MiniPluginItem extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_GIVE,
        COMMAND_CLEAR
    }

    MiniPluginCommand _pluginCommand;

    public MiniPluginItem(final HexusPlugin plugin) {
        super(plugin, "Item");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GIVE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_CLEAR);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandClear(this));
        _pluginCommand.register(new CommandGive(this));
    }

}
