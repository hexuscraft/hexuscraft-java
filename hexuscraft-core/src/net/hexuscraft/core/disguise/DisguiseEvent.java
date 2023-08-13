package net.hexuscraft.core.disguise;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DisguiseEvent extends Event {

    public final Player _player;
    public final GameProfile _originalProfile;
    public final GameProfile _newProfile;

    public DisguiseEvent(Player player, GameProfile originalProfile, GameProfile newProfile) {
        _player = player;
        _originalProfile = originalProfile;
        _newProfile = newProfile;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }


}
