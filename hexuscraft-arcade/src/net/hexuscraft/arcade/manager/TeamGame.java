package net.hexuscraft.arcade.manager;

import net.hexuscraft.common.enums.GameType;

import java.util.concurrent.atomic.AtomicReference;

public abstract class TeamGame extends Game
{

    public final GameTeam[] _teams;

    public final AtomicReference<GameState> _state = new AtomicReference<>();

    protected TeamGame(final GameType type, final GameKit[] kits, final GameTeam[] teams)
    {
        super(type, kits);
        _teams = teams;
    }

}
