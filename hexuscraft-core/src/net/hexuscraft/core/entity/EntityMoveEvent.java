package net.hexuscraft.core.entity;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

@SuppressWarnings("unused")
public class EntityMoveEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancel = false;

    private final Location _from;
    private final Location _to;

    public EntityMoveEvent(final Entity entity, final Location from, final Location to) {
        super(entity);
        _from = from;
        _to = to;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlerList() {
        return handlers;
    }

    public Location getFrom() {
        return _from;
    }

    public Location getTo() {
        return _to;
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
