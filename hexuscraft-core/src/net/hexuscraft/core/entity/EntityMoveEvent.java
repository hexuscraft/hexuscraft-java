package net.hexuscraft.core.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EntityMoveEvent extends Event implements Cancellable {

    public final Entity _entity;
    public final Location _from;
    public final Location _to;

    public EntityMoveEvent(final Entity entity, final Location from, final Location to) {
        _entity = entity;
        _from = from;
        _to = to;
    }

    private static final HandlerList _handlers = new HandlerList();
    private boolean _cancelled = false;

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    @Override
    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        _cancelled = cancelled;
    }

    public boolean isHorizontal(final boolean block) {
        if (block)
            return _from.getBlockX() != _to.getBlockX() || _from.getBlockZ() != _to.getBlockZ();
        return _from.getX() != _to.getX() || _from.getZ() != _to.getZ();
    }

    public boolean isVertical(final boolean block) {
        if (block)
            return _from.getBlockY() != _to.getBlockY();
        return _from.getY() != _to.getY();
    }

    public boolean isYaw() {
        return _from.getYaw() != _to.getYaw();
    }

    public boolean isPitch() {
        return _from.getPitch() != _to.getPitch();
    }

}
