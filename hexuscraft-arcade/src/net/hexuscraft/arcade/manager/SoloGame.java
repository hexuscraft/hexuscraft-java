package net.hexuscraft.arcade.manager;

import net.hexuscraft.common.enums.GameType;

import java.util.concurrent.atomic.AtomicReference;

public abstract class SoloGame extends Game
{

    public final AtomicReference<GameState> _state = new AtomicReference<>();

    protected SoloGame(final GameType type, final GameKit[] kits)
    {
        super(type, kits);
    }

}
