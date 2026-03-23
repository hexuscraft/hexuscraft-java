package net.hexuscraft.common.enums;

public enum ReportSubmitReason {

    CHAT("Chat Offense"),
    GAMEPLAY("Gameplay Offense"),
    CLIENT("Client Offense"),
    MISC("Miscellaneous");

    public final String _friendlyName;

    ReportSubmitReason(final String friendlyName) {
        _friendlyName = friendlyName;
    }

}
