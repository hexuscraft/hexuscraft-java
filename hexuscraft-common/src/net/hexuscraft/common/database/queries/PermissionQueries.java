package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;
import redis.clients.jedis.UnifiedJedis;

import java.util.Set;
import java.util.UUID;

public class PermissionQueries
{

    public static String GROUPS(UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "permission", "groups");
    }

    public static Set<String> getGroupNames(UnifiedJedis jedis, UUID uuid)
    {
        return jedis.smembers(GROUPS(uuid));
    }

}
