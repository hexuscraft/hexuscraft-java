package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class NewsUpdatedMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "news.updated";

    public final UUID _id;

    public NewsUpdatedMessage(final UUID id) {
        _id = id;
    }

    public static NewsUpdatedMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new NewsUpdatedMessage(UUID.fromString(jsonObject.getString("id")));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("id",
                _id.toString()))).toString();
    }

}
