package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.messages.NewsDeletedMessage;
import net.hexuscraft.common.database.messages.NewsUpdatedMessage;
import net.hexuscraft.common.database.queries.NewsQueries;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NewsData {

    public final UUID _id; // part of the key name
    public final boolean _active;
    public final int _weight;
    public final String _message;

    public NewsData(final UUID id, final Map<String, String> data) {
        _id = id;
        _active = Boolean.parseBoolean(data.get("active"));
        _weight = Integer.parseInt(data.get("weight"));
        _message = data.get("message");
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("active", Boolean.toString(_active));
        map.put("weight", Integer.toString(_weight));
        map.put("message", _message);
        return map;
    }

    public void publish(final UnifiedJedis jedis) {
        jedis.hset(NewsQueries.NEWS(_id), toMap());
        new NewsUpdatedMessage(_id).publish(jedis);
    }

    // TODO: /news delete
    @SuppressWarnings("unused")
    public void delete(final UnifiedJedis jedis) {
        jedis.del(NewsQueries.NEWS(_id));
        new NewsDeletedMessage(_id).publish(jedis);
    }

}
