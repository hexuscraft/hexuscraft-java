package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class PortalTeleportMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "portal.teleportOther";

    public final UUID _targetUUID;
    public final String _serverName;

    public PortalTeleportMessage(final UUID targetUUID, final String serverName) {
        this._targetUUID = targetUUID;
        this._serverName = serverName;
    }

    public static PortalTeleportMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportMessage(UUID.fromString(jsonObject.getString("targetUUID")), jsonObject.getString("serverName"));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()), Map.entry("serverName", _serverName))).toString();
    }

}
