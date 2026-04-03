package net.hexuscraft.core.actionbar;

import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public record ActionBar(Player player, AtomicInteger weight, AtomicReference<String> message)
{

    public ActionBar(Player player, int weight, String message)
    {
        this(player, new AtomicInteger(weight), new AtomicReference<>(message));
    }

}
