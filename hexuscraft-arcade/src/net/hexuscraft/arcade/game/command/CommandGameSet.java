package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameSet extends BaseCommand<MiniPluginGame> {

    CommandGameSet(final MiniPluginGame miniPluginGame) {
        super(miniPluginGame, "set", "<Name> [Map]", "Change the active game.", Set.of("change", "c"), MiniPluginGame.PERM.COMMAND_GAME_SET);
    }

}
