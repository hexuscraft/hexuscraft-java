package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class NewsDeletedMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "news.deleted";

    public final UUID _id;

    public NewsDeletedMessage(final UUID id) {
        this._id = id;
    }

    public static NewsDeletedMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new NewsDeletedMessage(UUID.fromString(jsonObject.getString("id")));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("id", _id.toString()))).toString();
    }

}
