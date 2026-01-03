package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record PortalTeleportOtherMessage(UUID _targetUniqueId, String _serverName, UUID _senderUniqueId) {

    public final static String CHANNEL_NAME = "portal.teleportOther";

    public static PortalTeleportOtherMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportOtherMessage(UUID.fromString(jsonObject.getString("targetUniqueId")),
                jsonObject.getString("serverName"), UUID.fromString(jsonObject.getString("senderUniqueId")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.of("targetUniqueId", _targetUniqueId.toString(), "serverName", _serverName),
                "senderUniqueId", _senderUniqueId.toString()).toString();
    }

}
