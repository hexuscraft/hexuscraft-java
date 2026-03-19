package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameStop extends BaseCommand<ArcadeGame> {

    CommandGameStop(final ArcadeGame arcadeGame) {
        super(arcadeGame,
                "stop",
                "",
                "Stop the currently active game.",
                Set.of(),
                ArcadeGame.PERM.COMMAND_GAME_STOP);
    }

}
