package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.PluginGame;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public class CommandGame extends BaseMultiCommand {

    protected CommandGame(PluginGame game) {
        super(game, "game", "Manage the active game.", Set.of("gm"), PluginGame.PERM.COMMAND_GAME, Set.of());
    }

}
