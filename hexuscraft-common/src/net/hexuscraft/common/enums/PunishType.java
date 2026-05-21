package net.hexuscraft.common.enums;

public enum PunishType {

    WARNING("Warning"),
    KICK("Kick"),
    MUTE("Mute"),
    BAN("Ban");

    public final String _friendlyName;

    PunishType(String friendlyName) {
        _friendlyName = friendlyName;
    }

}