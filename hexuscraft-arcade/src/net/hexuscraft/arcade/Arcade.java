package net.hexuscraft.arcade;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.arcade.gamelobby.MiniPluginGameLobby;
import net.hexuscraft.arcade.host.MiniPluginHost;
import net.hexuscraft.core.HexusPlugin;

public final class Arcade extends HexusPlugin {

    @Override
    public void load() {
        require(new MiniPluginGame(this));
        require(new MiniPluginGameLobby(this));
        require(new MiniPluginHost(this));
    }

}
