package net.hexuscraft.core.npc;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public final class EntityMoveEvent extends EntityEvent implements Cancellable {

    private static final HandlerList _handlerList = new HandlerList();
    public final Location _from;
    public final Location _to;
    private boolean _isCancelled;

    public EntityMoveEvent(final Entity entity, final Location from, final Location to) {
        super(entity);
        _from = from;
        _to = to;
        _isCancelled = false;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return _handlerList;
    }

    @Override
    public boolean isCancelled() {
        return _isCancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this._isCancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return _handlerList;
    }

    public boolean isAny() {
        if (_from == null) return false;
        if (_to == null) return false;
        return isHorizontal(false) || isVertical(false) || isYaw() || isPitch();
    }

    public boolean isHorizontal(final boolean block) {
        if (_from == null) return false;
        if (_to == null) return false;
        if (block) return _from.getBlockX() != _to.getBlockX() || _from.getBlockZ() != _to.getBlockZ();
        return _from.getX() != _to.getX() || _from.getZ() != _to.getZ();
    }

    public boolean isVertical(final boolean block) {
        if (_from == null) return false;
        if (_to == null) return false;
        if (block) return _from.getBlockY() != _to.getBlockY();
        return _from.getY() != _to.getY();
    }

    public boolean isHead() {
        return isYaw() || isPitch();
    }

    public boolean isYaw() {
        if (_from == null) return false;
        if (_to == null) return false;
        return _from.getYaw() != _to.getYaw();
    }

    public boolean isPitch() {
        if (_from == null) return false;
        if (_to == null) return false;
        return _from.getPitch() != _to.getPitch();
    }

}
