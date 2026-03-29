package net.hexuscraft.arcade.manager.event;

import net.hexuscraft.arcade.manager.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameStateChangedEvent extends Event implements Cancellable
{

    private static final HandlerList _handlers = new HandlerList();
    public GameState _oldState;
    public GameState _newState;
    private boolean _isCancelled;

    public GameStateChangedEvent(GameState oldState, GameState newState)
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
    public void setCancelled(boolean isCancelled)
    {
        _isCancelled = isCancelled;
    }

}
