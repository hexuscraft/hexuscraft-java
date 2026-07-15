package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import redis.clients.jedis.UnifiedJedis;

import java.util.*;

public class ServerQueries {

	public static String SERVER(String name) {
		return Database.buildQuery("server", name);
	}

	public static String SERVERGROUP(String name) {
		return Database.buildQuery("servergroup", name);
	}

	public static String MOTD() {
		return Database.buildQuery("motd");
	}

	public static ServerData getServer(UnifiedJedis jedis, String id) {
		Map<String, String> dataMap = jedis.hgetAll(SERVER(id));
		if (dataMap.isEmpty()) {
			return null;
		}
		return new ServerData(id, dataMap);
	}

	public static ServerData[] getServers(UnifiedJedis jedis) {
		Set<ServerData> serverDataSet = new HashSet<>();
		jedis.keys(SERVER("*"))
			.forEach(key -> serverDataSet.add(getServer(jedis, key.split(Database.KEY_DELIMITER, 2)[1])));
		return serverDataSet.toArray(ServerData[]::new);
	}

	public static ServerData[] getServers(UnifiedJedis jedis, String serverGroupName) {
		return Arrays.stream(getServers(jedis))
			.filter(serverData -> serverData._group.equals(serverGroupName))
			.toArray(ServerData[]::new);
	}

	public static ServerData[] getServers(UnifiedJedis jedis, ServerGroupData serverGroupData) {
		return getServers(jedis, serverGroupData._id);
	}

	public static Map<String, ServerData> getServersAsMap(UnifiedJedis jedis) {
		Map<String, ServerData> serverDataMap = new HashMap<>();
		for (ServerData serverData : getServers(jedis)) {
			serverDataMap.put(serverData._id, serverData);
		}
		return serverDataMap;
	}

	public static ServerGroupData getServerGroup(UnifiedJedis jedis, String id) {
		Map<String, String> dataMap = jedis.hgetAll(SERVERGROUP(id));
		if (dataMap.isEmpty()) {
			return null;
		}
		return new ServerGroupData(id, dataMap);
	}

	public static ServerGroupData[] getServerGroups(UnifiedJedis jedis) {
		Set<ServerGroupData> serverGroupDataSet = new HashSet<>();
		jedis.keys(SERVERGROUP("*"))
			.forEach(key -> serverGroupDataSet.add(getServerGroup(jedis,
				key.split(Database.KEY_DELIMITER, 2)[1])));
		return serverGroupDataSet.toArray(ServerGroupData[]::new);
	}

	public static Map<String, ServerGroupData> getServerGroupsAsMap(UnifiedJedis jedis) {
		Map<String, ServerGroupData> serverGroupDataMap = new HashMap<>();
		for (ServerGroupData serverGroupData : getServerGroups(jedis)) {
			serverGroupDataMap.put(serverGroupData._id, serverGroupData);
		}
		return serverGroupDataMap;
	}

	public static String getMotd(UnifiedJedis jedis) {
		return jedis.get(MOTD());
	}

	public static void setMotd(UnifiedJedis jedis, String value) {
		jedis.set(MOTD(), value);
	}

}
