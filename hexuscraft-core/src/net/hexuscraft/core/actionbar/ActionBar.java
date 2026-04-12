package net.hexuscraft.core.actionbar;

import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ActionBar
{
    public final Player _player;
    private final CoreActionBar _coreActionBar;
    private final AtomicInteger _weight;
    private final AtomicReference<String> _message;

    public ActionBar(CoreActionBar coreActionBar, Player player, int weight, String message)
    {
        _coreActionBar = coreActionBar;
        _player = player;
        _weight = new AtomicInteger(weight);
        _message = new AtomicReference<>(message);
    }

    public int getWeight()
    {
        return _weight.get();
    }

    public String getMessage()
    {
        return _message.get();
    }

    public int setWeight(int weight)
    {
        int oldWeight = _weight.getAndSet(weight);
        _coreActionBar.updateActionBars();
        return oldWeight;
    }

    public String setMessage(String message)
    {
        String oldMessage = _message.getAndSet(message);
        _coreActionBar.updateActionBars();
        return oldMessage;
    }

}
