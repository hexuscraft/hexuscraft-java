package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class NewsDeletedMessage extends BaseMessage
{

    public static String CHANNEL_NAME = "news.deleted";

    public UUID _id;

    public NewsDeletedMessage(UUID id)
    {
        _id = id;
    }

    public static NewsDeletedMessage parse(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new NewsDeletedMessage(UUID.fromString(jsonObject.getString("id")));
    }

    public String stringify()
    {
        return new JSONObject(Map.ofEntries(Map.entry("id", _id.toString()))).toString();
    }

}
