package net.hexuscraft.common.enums;

public enum ReportSubmitReason
{

    CHAT("Chat Offense"),
    GAMEPLAY("Gameplay Offense"),
    CLIENT("Client Offense"),
    MISC("Miscellaneous");

    public String _friendlyName;

    ReportSubmitReason(String friendlyName)
    {
        _friendlyName = friendlyName;
    }

}
