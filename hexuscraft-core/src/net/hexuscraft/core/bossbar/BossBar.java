package net.hexuscraft.core.bossbar;

import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class BossBar {

    final Player _player;
    final AtomicInteger _weight;
    final AtomicReference<String> _message;
    final AtomicReference<Float> _progress;

    public BossBar(final Player player, final int weight, final String message, final float progress) {
        _player = player;
        _weight = new AtomicInteger(weight);
        _message = new AtomicReference<>(message);
        _progress = new AtomicReference<>(progress);
    }

}
