package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record SupportMessage(UUID _senderUniqueId, String _message, String _serverName) {

    public final static String CHANNEL_NAME = "chat.support";

    public static SupportMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new SupportMessage(UUID.fromString(jsonObject.getString("senderUniqueId")),
                jsonObject.getString("message"), jsonObject.getString("serverName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.of("senderUniqueId", _senderUniqueId.toString(), "message", _message, "serverName",
                _serverName)).toString();
    }

}
