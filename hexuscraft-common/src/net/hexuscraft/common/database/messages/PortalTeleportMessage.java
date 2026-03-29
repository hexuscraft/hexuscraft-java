package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class PortalTeleportMessage extends BaseMessage
{

    public static String CHANNEL_NAME = "portal.teleport";

    public UUID _targetUUID;
    public String _serverName;

    public PortalTeleportMessage(UUID targetUUID, String serverName)
    {
        this._targetUUID = targetUUID;
        this._serverName = serverName;
    }

    public static PortalTeleportMessage parse(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportMessage(UUID.fromString(jsonObject.getString("targetUUID")),
                jsonObject.getString("serverName"));
    }

    public String stringify()
    {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()),
                Map.entry("serverName", _serverName))).toString();
    }

}
