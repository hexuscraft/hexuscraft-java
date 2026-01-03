package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record PortalTeleportMessage(UUID _uniqueId, String _serverName) {

    public final static String CHANNEL_NAME = "portal.teleport";

    public static PortalTeleportMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PortalTeleportMessage(UUID.fromString(jsonObject.getString("uniqueId")),
                jsonObject.getString("serverName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.of("uniqueId", _uniqueId.toString(), "serverName", _serverName)).toString();
    }

}
