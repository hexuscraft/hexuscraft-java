package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class PortalTeleportStaffMessage extends BaseMessage
{

    public static String CHANNEL_NAME = "portal.teleportStaff";

    public UUID _targetUUID;
    public String _serverName;
    public UUID _senderUUID;

    public PortalTeleportStaffMessage(UUID targetUUID, String serverName, UUID senderUUID)
    {
        _targetUUID = targetUUID;
        _serverName = serverName;
        _senderUUID = senderUUID;
    }

    public static PortalTeleportStaffMessage parse(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportStaffMessage(UUID.fromString(jsonObject.getString("targetUUID")),
                jsonObject.getString("serverName"),
                UUID.fromString(jsonObject.getString("senderUUID")));
    }

    public String stringify()
    {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()),
                Map.entry("serverName", _serverName),
                Map.entry("senderUUID", _senderUUID.toString()))).toString();
    }

}
