package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.command.CommandGame;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public class PluginGame extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_GAME,
        COMMAND_GAME_SET
    }

    private PluginCommand _command;

    public PluginGame(Arcade arcade) {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME_SET);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _command = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _command.register(new CommandGame(this));
    }
}
