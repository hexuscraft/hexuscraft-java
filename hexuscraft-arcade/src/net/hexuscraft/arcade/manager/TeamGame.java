package net.hexuscraft.arcade.manager;

import net.hexuscraft.common.enums.GameType;

import java.util.concurrent.atomic.AtomicReference;

public abstract class TeamGame extends Game
{

    public GameTeam[] _teams;

    public AtomicReference<GameState> _state = new AtomicReference<>();

    protected TeamGame(GameType type, GameKit[] kits, GameTeam[] teams)
    {
        super(type, kits);
        _teams = teams;
    }

}
