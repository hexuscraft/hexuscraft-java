package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.PluginGame;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public class CommandGame extends BaseMultiCommand {

    public CommandGame(PluginGame pluginGame) {
        super(pluginGame, "game", "Manage the active game.", Set.of("arcade"), PluginGame.PERM.COMMAND_GAME, Set.of(
                new CommandGameSet(pluginGame)
        ));
    }

}
