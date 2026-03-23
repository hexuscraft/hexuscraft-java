package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.arcade.manager.GameKit;
import net.hexuscraft.arcade.manager.GameTeam;
import net.hexuscraft.arcade.manager.TeamGame;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.utils.C;

public final class GameSurvivalGamesDuo extends TeamGame {

    private GameSurvivalGamesDuo(final ArcadeManager arcadeManager) {
        super(GameType.SURVIVAL_GAMES, new GameKit[]{}, new GameTeam[]{
                new GameTeam("Black", C.cBlack),
                new GameTeam("Blue", C.cDBlue),
                new GameTeam("Green", C.cDGreen),
                new GameTeam("Aqua", C.cDAqua),
                new GameTeam("Red", C.cDRed),
                new GameTeam("Purple", C.cDPurple),
                new GameTeam("", C.cDPurple),
        });
    }

}
