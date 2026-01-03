package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;

public record PortalRestartServerMessage(String _serverName) {

    public final static String CHANNEL_NAME = "portal.restartServer";

    public static PortalRestartServerMessage fromString(final String jsonString) {
        return new PortalRestartServerMessage(new JSONObject(jsonString).getString("serverName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.of("serverName", _serverName)).toString();
    }

}
