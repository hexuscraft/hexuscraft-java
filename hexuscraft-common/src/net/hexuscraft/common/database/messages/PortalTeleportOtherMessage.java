package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class PortalTeleportOtherMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "portal.teleportOther";

    public final UUID _targetUUID;
    public final String _serverName;
    public final UUID _senderUUID;

    public PortalTeleportOtherMessage(final UUID targetUUID, final String serverName, final UUID senderUUID) {
        this._targetUUID = targetUUID;
        this._serverName = serverName;
        this._senderUUID = senderUUID;
    }

    public static PortalTeleportOtherMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportOtherMessage(UUID.fromString(jsonObject.getString("targetUUID")), jsonObject.getString("serverName"), UUID.fromString(jsonObject.getString("senderUUID")));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()), Map.entry("serverName", _serverName), Map.entry("senderUUID", _senderUUID.toString()))).toString();
    }

}
