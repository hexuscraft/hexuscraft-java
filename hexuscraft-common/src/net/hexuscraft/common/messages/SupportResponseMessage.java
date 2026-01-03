package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record SupportResponseMessage(UUID _senderUniqueId, UUID _targetUniqueId, String _message) {

    public final static String CHANNEL_NAME = "chat.supportResponse";

    public static SupportResponseMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new SupportResponseMessage(UUID.fromString(jsonObject.getString("senderUniqueId")),
                UUID.fromString(jsonObject.getString("targetUniqueId")), jsonObject.getString("message"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(
                Map.of("senderUniqueId", _senderUniqueId.toString(), "targetUniqueId", _targetUniqueId.toString(),
                        "message", _message)).toString();
    }

}
