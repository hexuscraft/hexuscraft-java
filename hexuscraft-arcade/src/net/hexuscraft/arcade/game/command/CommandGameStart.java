package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameStart extends BaseCommand<MiniPluginGame> {

    CommandGameStart(final MiniPluginGame miniPluginGame) {
        super(miniPluginGame, "start", "[Seconds]", "Start the currently loaded game.", Set.of(),
                MiniPluginGame.PERM.COMMAND_GAME_START);
    }

}
