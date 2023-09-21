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
        COMMAND_GAME
    }

    protected PluginGame(Arcade arcade) {
        super(arcade, "Game");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAME);
    }

    @Override
    public final void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        final PluginCommand pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        pluginCommand.register(new CommandGame(this));
    }
}
