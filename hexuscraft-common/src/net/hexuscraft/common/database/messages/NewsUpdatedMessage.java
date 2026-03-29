package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class NewsUpdatedMessage extends BaseMessage
{

    public static String CHANNEL_NAME = "news.updated";

    public UUID _id;

    public NewsUpdatedMessage(UUID id)
    {
        _id = id;
    }

    public static NewsUpdatedMessage parse(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new NewsUpdatedMessage(UUID.fromString(jsonObject.getString("id")));
    }

    public String stringify()
    {
        return new JSONObject(Map.ofEntries(Map.entry("id", _id.toString()))).toString();
    }

}
