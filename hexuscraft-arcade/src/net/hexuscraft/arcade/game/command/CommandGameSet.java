package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.game.PluginGame;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public class CommandGameSet extends BaseCommand<Arcade> {

    protected CommandGameSet(PluginGame pluginGame) {
        super(pluginGame, "set", "<Name> [Map]", "Change the active game.", Set.of("change", "c"), PluginGame.PERM.COMMAND_GAME_SET);
    }

}
