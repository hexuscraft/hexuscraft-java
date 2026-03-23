package net.hexuscraft.arcade;

import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.arcade.lobby.ArcadeLobby;
import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.core.HexusPlugin;

public final class Arcade extends HexusPlugin {

    public Arcade() {
        super();

        require(new ArcadeHost(this));
        require(new ArcadeLobby(this));
        require(new ArcadeManager(this));
    }

}
