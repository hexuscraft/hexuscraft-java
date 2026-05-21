package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record SalesProcessedMessage(UUID _playerUUID, String _packageName) {

    public static String CHANNEL_NAME = "sales.processed";

    public static SalesProcessedMessage fromString(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new SalesProcessedMessage(UUID.fromString(jsonObject.getString("playerUUID")),
                jsonObject.getString("packageName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.ofEntries(Map.entry("playerUUID", _playerUUID.toString()),
                Map.entry("packageName", _packageName))).toString();
    }

}
