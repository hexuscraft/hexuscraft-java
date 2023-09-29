package net.hexuscraft.arcade;

import net.hexuscraft.arcade.game.PluginGame;
import net.hexuscraft.core.HexusPlugin;

public class Arcade extends HexusPlugin {

    @Override
    public final void load() {
        require(new PluginGame(this));
    }

}
