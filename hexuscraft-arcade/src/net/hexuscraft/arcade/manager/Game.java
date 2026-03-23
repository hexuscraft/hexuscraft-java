package net.hexuscraft.arcade.manager;

import net.hexuscraft.common.enums.GameType;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Game {

    public final GameType _type;
    public final GameKit[] _kits;

    public final AtomicReference<GameState> _state = new AtomicReference<>();

    protected Game(final GameType type, final GameKit[] kits) {
        _type = type;
        _kits = kits;
    }

}
