package net.hexuscraft.core.bossbar;

import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public record BossBar(Player player, AtomicInteger weight, AtomicReference<String> message)
{

    public BossBar(Player player, int weight, String message)
    {
        this(player, new AtomicInteger(weight), new AtomicReference<>(message));
    }

}
