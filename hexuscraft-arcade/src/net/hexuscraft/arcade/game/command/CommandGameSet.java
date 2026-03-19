package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameSet extends BaseCommand<ArcadeGame> {

    CommandGameSet(final ArcadeGame arcadeGame) {
        super(arcadeGame,
                "set",
                "<Name> [Map]",
                "Change the active game.",
                Set.of("change",
                        "c"),
                ArcadeGame.PERM.COMMAND_GAME_SET);
    }

}
