package net.hexuscraft.core.gamemode;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.gamemode.command.CommandGameMode;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public class PluginGameMode extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_GAMEMODE,
        COMMAND_GAMEMODE_OTHERS
    }

    PluginCommand _pluginCommand;

    public PluginGameMode(final HexusPlugin plugin) {
        super(plugin, "Game Mode");

        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_GAMEMODE);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAMEMODE_OTHERS);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandGameMode(this));
    }

}
