package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class ServerQueries {

    public static String SERVER(final String name) {
        return Database.buildQuery("server", name);
    }

    public static String SERVERGROUP(String name) {
        return Database.buildQuery("servergroup", name);
    }

    public static String MOTD() {
        return Database.buildQuery("motd");
    }

    public static ServerData getServer(final UnifiedJedis jedis, final String serverName) {
        final Map<String, String> dataMap = jedis.hgetAll(SERVER(serverName));
        if (dataMap.isEmpty()) return null;
        return new ServerData(serverName, dataMap);
    }

    public static ServerData[] getServers(final UnifiedJedis jedis) {
        final Set<ServerData> serverDataSet = new HashSet<>();
        jedis.keys(Database.buildQuery("server", "*"))
                .forEach(key -> serverDataSet.add(getServer(jedis, key.split(Database.KEY_DELIMITER, 2)[1])));
        return serverDataSet.toArray(ServerData[]::new);
    }

    public static ServerData[] getServers(final UnifiedJedis jedis, final String serverGroupName) {
        final Set<ServerData> serverDataSet = new HashSet<>();
        for (ServerData serverData : getServers(jedis)) {
            if (serverData._group.equals(serverGroupName)) {
                serverDataSet.add(serverData);
            }
        }
        return serverDataSet.toArray(ServerData[]::new);
    }

    public static ServerData[] getServers(final UnifiedJedis jedis, final ServerGroupData serverGroupData) {
        return getServers(jedis, serverGroupData._name);
    }

    public static Map<String, ServerData> getServersAsMap(final UnifiedJedis jedis) {
        final Map<String, ServerData> serverDataMap = new HashMap<>();
        for (ServerData serverData : getServers(jedis)) {
            serverDataMap.put(serverData._name, serverData);
        }
        return serverDataMap;
    }

    public static ServerGroupData getServerGroup(final UnifiedJedis jedis, final String name) {
        final Map<String, String> dataMap = jedis.hgetAll(SERVERGROUP(name));
        if (dataMap.isEmpty()) return null;
        return new ServerGroupData(name, dataMap);
    }

    public static ServerGroupData[] getServerGroups(final UnifiedJedis jedis) {
        final Set<ServerGroupData> serverGroupDataSet = new HashSet<>();
        jedis.keys(Database.buildQuery("servergroup", "*"))
                .forEach(key -> serverGroupDataSet.add(getServerGroup(jedis, key.split(Database.KEY_DELIMITER, 2)[1])));
        return serverGroupDataSet.toArray(ServerGroupData[]::new);
    }

    public static Map<String, ServerGroupData> getServerGroupsAsMap(final UnifiedJedis jedis) {
        final Map<String, ServerGroupData> serverGroupDataMap = new HashMap<>();
        for (ServerGroupData serverGroupData : getServerGroups(jedis)) {
            serverGroupDataMap.put(serverGroupData._name, serverGroupData);
        }
        return serverGroupDataMap;
    }

    public static String getMotd(final UnifiedJedis jedis) {
        return jedis.get(MOTD());
    }

    public static void setMotd(final UnifiedJedis jedis, final String value) {
        jedis.set(MOTD(), value);
    }

}
