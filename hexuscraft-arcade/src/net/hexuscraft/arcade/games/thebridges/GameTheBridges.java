package net.hexuscraft.arcade.games.thebridges;

import net.hexuscraft.arcade.game.Game;
import net.hexuscraft.arcade.game.GameTeam;
import org.bukkit.Color;

public final class GameTheBridges extends Game {

    private GameTheBridges() {
        super("The Bridges", new GameTeam[]{
                new GameTeam("Red", Color.RED),
                new GameTeam("Blue", Color.AQUA),
                new GameTeam("Green", Color.LIME),
                new GameTeam("Yellow", Color.YELLOW),
        });
    }

}
