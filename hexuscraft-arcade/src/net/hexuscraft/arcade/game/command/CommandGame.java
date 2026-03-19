package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public final class CommandGame extends BaseMultiCommand<ArcadeGame> {

    public CommandGame(final ArcadeGame arcadeGame) {
        super(arcadeGame,
                "game",
                "Manage the active game.",
                Set.of("arcade"),
                ArcadeGame.PERM.COMMAND_GAME,
                Set.of(new CommandGameSet(arcadeGame),
                        new CommandGameStart(arcadeGame),
                        new CommandGameStop(arcadeGame)));
    }

}
