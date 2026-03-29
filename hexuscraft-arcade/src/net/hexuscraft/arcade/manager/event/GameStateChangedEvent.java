package net.hexuscraft.arcade.manager.event;

import net.hexuscraft.arcade.manager.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class GameStateChangedEvent extends Event implements Cancellable
{

    private static final HandlerList _handlers = new HandlerList();
    public final GameState _oldState;
    public final GameState _newState;
    private boolean _isCancelled;

    public GameStateChangedEvent(final GameState oldState, final GameState newState)
    {
        _oldState = oldState;
        _newState = newState;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return _isCancelled;
    }

    @Override
    public void setCancelled(final boolean isCancelled)
    {
        _isCancelled = isCancelled;
    }

}
