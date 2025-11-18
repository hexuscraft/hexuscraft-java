package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameStop extends BaseCommand<MiniPluginGame> {

    CommandGameStop(final MiniPluginGame miniPluginGame) {
        super(miniPluginGame, "stop", "", "Stop the currently active game.", Set.of(), MiniPluginGame.PERM.COMMAND_GAME_STOP);
    }

}
