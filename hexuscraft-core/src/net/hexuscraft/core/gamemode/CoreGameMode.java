package net.hexuscraft.core.gamemode;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.gamemode.command.CommandGameMode;

import java.util.Map;

public final class CoreGameMode extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_GAMEMODE,
        COMMAND_GAMEMODE_OTHERS
    }

    private CoreCommand _coreCommand;

    public CoreGameMode(final HexusPlugin plugin) {
        super(plugin, "Game Mode");

        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_GAMEMODE);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAMEMODE_OTHERS);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable() {
        _coreCommand.register(new CommandGameMode(this));
    }

}
