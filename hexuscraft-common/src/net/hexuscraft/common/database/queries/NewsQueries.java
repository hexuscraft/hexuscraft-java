package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.data.NewsData;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NewsQueries
{

    public static String NEWS(UUID id)
    {
        return Database.buildQuery("news", id.toString());
    }

    public static NewsData getNews(UnifiedJedis jedis, UUID id)
    {
        Map<String, String> dataMap = jedis.hgetAll(NEWS(id));
        if (dataMap.isEmpty())
        {
            return null;
        }
        return new NewsData(id, dataMap);
    }

    public static NewsData[] getNews(UnifiedJedis jedis)
    {
        Set<NewsData> news = new HashSet<>();
        jedis.keys(Database.buildQuery("news", "*"))
                .forEach(key -> news.add(getNews(jedis, UUID.fromString(key.split(Database.KEY_DELIMITER, 2)[1]))));
        return news.toArray(NewsData[]::new);
    }

}
