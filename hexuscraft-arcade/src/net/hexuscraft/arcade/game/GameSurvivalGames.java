package net.hexuscraft.arcade.game;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.arcade.manager.GameKit;
import net.hexuscraft.arcade.manager.SoloGame;
import net.hexuscraft.common.enums.GameType;

public final class GameSurvivalGames extends SoloGame {

    public int _minPlayers = 1;
    public int _maxPlayers = 1;

    private GameSurvivalGames(final ArcadeManager arcadeManager) {
        super(GameType.SURVIVAL_GAMES, new GameKit[]{});
    }

}
