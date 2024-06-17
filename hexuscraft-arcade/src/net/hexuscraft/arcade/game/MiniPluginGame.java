package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.command.CommandGame;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public final class MiniPluginGame extends MiniPlugin<Arcade> {

    public enum PERM implements IPermission {
        COMMAND_GAME,
        COMMAND_GAME_SET
    }

    private MiniPluginCommand _command;

    public MiniPluginGame(final Arcade arcade) {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME_SET);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _command = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _command.register(new CommandGame(this));
    }
}
