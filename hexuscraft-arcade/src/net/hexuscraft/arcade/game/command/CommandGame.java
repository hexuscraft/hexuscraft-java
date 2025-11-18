package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public final class CommandGame extends BaseMultiCommand<MiniPluginGame> {

    public CommandGame(final MiniPluginGame miniPluginGame) {
        super(miniPluginGame, "game", "Manage the active game.", Set.of("arcade"), MiniPluginGame.PERM.COMMAND_GAME, Set.of(new CommandGameSet(miniPluginGame), new CommandGameStart(miniPluginGame), new CommandGameStop(miniPluginGame)));
    }

}
