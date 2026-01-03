package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;
import redis.clients.jedis.UnifiedJedis;

import java.util.Set;
import java.util.UUID;

public final class PermissionQueries {

    public static String GROUPS(final UUID uuid) {
        return Database.buildQuery("user", uuid.toString(), "permission", "groups");
    }

    public static Set<String> getGroupNames(final UnifiedJedis jedis, final UUID uuid) {
        return jedis.smembers(GROUPS(uuid));
    }

}
