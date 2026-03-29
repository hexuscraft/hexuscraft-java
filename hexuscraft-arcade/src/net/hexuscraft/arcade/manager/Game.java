package net.hexuscraft.arcade.manager;

import net.hexuscraft.common.enums.GameType;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Game
{

    public GameType _type;
    public GameKit[] _kits;

    public AtomicReference<GameState> _state = new AtomicReference<>();

    protected Game(GameType type, GameKit[] kits)
    {
        _type = type;
        _kits = kits;
    }

}
