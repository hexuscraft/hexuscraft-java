package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameStart extends BaseCommand<ArcadeGame> {

    CommandGameStart(final ArcadeGame arcadeGame) {
        super(arcadeGame,
                "start",
                "[Seconds]",
                "Start the currently loaded game.",
                Set.of(),
                ArcadeGame.PERM.COMMAND_GAME_START);
    }

}
