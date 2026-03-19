package net.hexuscraft.arcade;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.arcade.gamelobby.ArcadeGameLobby;
import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.core.HexusPlugin;

public final class Arcade extends HexusPlugin {

    public Arcade() {
        super();

        require(new ArcadeGame(this));
        require(new ArcadeGameLobby(this));
        require(new ArcadeHost(this));
    }

}
