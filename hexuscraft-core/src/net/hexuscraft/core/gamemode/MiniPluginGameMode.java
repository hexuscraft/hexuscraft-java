package net.hexuscraft.core.gamemode;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.gamemode.command.CommandGameMode;

import java.util.Map;

public final class MiniPluginGameMode extends MiniPlugin<HexusPlugin> {

    MiniPluginCommand _pluginCommand;

    public MiniPluginGameMode(final HexusPlugin plugin) {
        super(plugin, "Game Mode");

        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_GAMEMODE);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAMEMODE_OTHERS);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandGameMode(this));
    }

    public enum PERM implements IPermission {
        COMMAND_GAMEMODE,
        COMMAND_GAMEMODE_OTHERS
    }

}
