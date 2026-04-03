package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.arcade.manager.GameKit;
import net.hexuscraft.arcade.manager.GameTeam;
import net.hexuscraft.arcade.manager.TeamGame;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.utils.C;

public class GameTheBridges extends TeamGame
{

    GameTheBridges(ArcadeManager arcadeManager)
    {
        super(GameType.THE_BRIDGES,
                new GameKit[]{},
                new GameTeam[]{new GameTeam("Red", C.cRed),
                        new GameTeam("Blue", C.cAqua),
                        new GameTeam("Green", C.cGreen),
                        new GameTeam("Yellow", C.cYellow),});
    }

}
