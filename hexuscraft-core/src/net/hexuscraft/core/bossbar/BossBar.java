package net.hexuscraft.core.bossbar;

import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BossBar {

    public final AtomicInteger _weight;
    public final AtomicReference<String> _message;
    public final AtomicReference<Float> _progress;
    final Player _player;
    final Wither _entity;

    BossBar(final Player player, final Wither entity) {
        _player = player;
        _entity = entity;
        _weight = new AtomicInteger(0);
        _message = new AtomicReference<>("");
        _progress = new AtomicReference<>(1F);
    }

}
