package net.hexuscraft.arcade.game.thebridges;

import net.hexuscraft.arcade.game.Game;
import net.hexuscraft.arcade.game.GameTeam;
import org.bukkit.Color;

@SuppressWarnings("unused")
public class GameTheBridges extends Game {

    protected GameTheBridges() {
        super("The Bridges", new GameTeam[]{
                new GameTeam("Red", Color.RED),
                new GameTeam("Blue", Color.AQUA),
                new GameTeam("Green", Color.LIME),
                new GameTeam("Yellow", Color.YELLOW),
        });
    }

}
