package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.messages.NewsDeletedMessage;
import net.hexuscraft.common.database.messages.NewsUpdatedMessage;
import net.hexuscraft.common.database.queries.NewsQueries;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewsData
{

    public UUID _id; // part of the key name
    public boolean _active;
    public int _weight;
    public String _message;

    public NewsData(UUID id, Map<String, String> data)
    {
        _id = id;
        _active = Boolean.parseBoolean(data.get("active"));
        _weight = Integer.parseInt(data.get("weight"));
        _message = data.get("message");
    }

    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("active", Boolean.toString(_active));
        map.put("weight", Integer.toString(_weight));
        map.put("message", _message);
        return map;
    }

    public void publish(UnifiedJedis jedis)
    {
        jedis.hset(NewsQueries.NEWS(_id), toMap());
        jedis.publish(NewsUpdatedMessage.CHANNEL_NAME, new NewsUpdatedMessage(_id).stringify());
    }

    public void delete(UnifiedJedis jedis)
    {
        jedis.del(NewsQueries.NEWS(_id));
        jedis.publish(NewsDeletedMessage.CHANNEL_NAME, new NewsDeletedMessage(_id).stringify());
    }

}
