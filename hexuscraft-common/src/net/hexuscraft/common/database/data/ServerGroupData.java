package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import redis.clients.jedis.UnifiedJedis;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class ServerGroupData extends IServerGroupData {
	public ServerGroupData(Map<String, String> serverGroupData) {
		assert serverGroupData.containsKey("name");
		_name = serverGroupData.get("name");

		if (serverGroupData.containsKey("capacity")) {
			_capacity = Integer.parseInt(serverGroupData.get("capacity"));
		}

		if (serverGroupData.containsKey("games")) {
			_games = Arrays.stream(serverGroupData.get("games").split(","))
				.filter(s -> !s.trim().isEmpty())
				.map(GameType::valueOf)
				.toArray(GameType[]::new);
		}

		if (serverGroupData.containsKey("hostUUID")) {
			_hostUUID = UUID.fromString(serverGroupData.get("hostUUID"));
		}

		if (serverGroupData.containsKey("joinableServers")) {
			_joinableServers = Integer.parseInt(serverGroupData.get("joinableServers"));
		}

		assert serverGroupData.containsKey("maxPort");
		_maxPort = Integer.parseInt(serverGroupData.get("maxPort"));

		assert serverGroupData.containsKey("minPort");
		_minPort = Integer.parseInt(serverGroupData.get("minPort"));

		assert serverGroupData.containsKey("plugin");
		_plugin = serverGroupData.get("plugin");

		if (serverGroupData.containsKey("ram")) {
			_ram = Integer.parseInt(serverGroupData.get("ram"));
		}

		if (serverGroupData.containsKey("requiredPermission")) {
			_requiredPermission = PermissionGroup.valueOf(serverGroupData.get("requiredPermission"));
		}

		if (serverGroupData.containsKey("totalServers")) {
			_totalServers = Integer.parseInt(serverGroupData.get("totalServers"));
		}

		if (serverGroupData.containsKey("timeoutMillis")) {
			_timeoutMillis = Integer.parseInt(serverGroupData.get("timeoutMillis"));
		}

		if (serverGroupData.containsKey("viaVersion")) {
			_viaVersion = Boolean.parseBoolean(serverGroupData.get("viaVersion"));
		}

		if (serverGroupData.containsKey("worldEdit")) {
			_worldEdit = Boolean.parseBoolean(serverGroupData.get("worldEdit"));
		}

		if (serverGroupData.containsKey("worldZip")) {
			_worldZip = serverGroupData.get("worldZip");
		}
	}

	public ServerGroupData(String name,
	                       int capacity,
	                       GameType[] games,
	                       UUID hostUUID,
	                       int joinableServers,
	                       int maxPort,
	                       int minPort,
	                       String plugin,
	                       int ram,
	                       PermissionGroup requiredPermission,
	                       int totalServers,
	                       int timeoutMillis,
	                       boolean viaVersion,
	                       boolean worldEdit,
	                       String worldZip) {
		_name = name;
		_capacity = capacity;
		_games = games;
		_hostUUID = hostUUID;
		_joinableServers = joinableServers;
		_maxPort = maxPort;
		_minPort = minPort;
		_plugin = plugin;
		_ram = ram;
		_requiredPermission = requiredPermission;
		_totalServers = totalServers;
		_timeoutMillis = timeoutMillis;
		_viaVersion = viaVersion;
		_worldEdit = worldEdit;
		_worldZip = worldZip;
	}

	public Map<String, String> toMap() {
		return Map.ofEntries(Map.entry("name", _name),
			Map.entry("capacity", Integer.toString(_capacity)),
			Map.entry("games", String.join(",", Arrays.stream(_games).map(GameType::name).toArray(String[]::new))),
			Map.entry("hostUUID", _hostUUID.toString()),
			Map.entry("joinableServers", Integer.toString(_joinableServers)),
			Map.entry("maxPort", Integer.toString(_maxPort)),
			Map.entry("minPort", Integer.toString(_minPort)),
			Map.entry("plugin", _plugin),
			Map.entry("ram", Integer.toString(_ram)),
			Map.entry("requiredPermission", _requiredPermission.name()),
			Map.entry("totalServers", Integer.toString(_totalServers)),
			Map.entry("timeoutMillis", Integer.toString(_timeoutMillis)),
			Map.entry("viaVersion", Boolean.toString(_viaVersion)),
			Map.entry("worldEdit", Boolean.toString(_worldEdit)),
			Map.entry("worldZip", _worldZip));
	}

	public void update(UnifiedJedis jedis) {
		jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
	}

	public ServerData[] getServers(UnifiedJedis jedis) {
		return ServerQueries.getServers(jedis, this);
	}

	public static class Builder extends IServerGroupData {
		public Builder name(String value) {
			_name = value;
			return this;
		}

		public Builder capacity(int value) {
			_capacity = value;
			return this;
		}

		public Builder games(GameType[] value) {
			_games = value;
			return this;
		}

		public Builder hostUUID(UUID value) {
			_hostUUID = value;
			return this;
		}

		public Builder joinableServers(int value) {
			_joinableServers = value;
			return this;
		}

		public Builder maxPort(int value) {
			_maxPort = value;
			return this;
		}

		public Builder minPort(int value) {
			_minPort = value;
			return this;
		}

		public Builder plugin(String value) {
			_plugin = value;
			return this;
		}

		public Builder ram(int value) {
			_ram = value;
			return this;
		}

		public Builder requiredPermission(PermissionGroup value) {
			_requiredPermission = value;
			return this;
		}

		public Builder totalServers(int value) {
			_totalServers = value;
			return this;
		}

		public Builder timeoutMillis(int value) {
			_timeoutMillis = value;
			return this;
		}

		public Builder viaVersion(boolean value) {
			_viaVersion = value;
			return this;
		}

		public Builder worldEdit(boolean value) {
			_worldEdit = value;
			return this;
		}

		public Builder worldZip(String value) {
			_worldZip = value;
			return this;
		}

		public ServerGroupData build() {
			return new ServerGroupData(_name,
				_capacity,
				_games,
				_hostUUID,
				_joinableServers,
				_maxPort,
				_minPort,
				_plugin,
				_ram,
				_requiredPermission,
				_totalServers,
				_timeoutMillis,
				_viaVersion,
				_worldEdit,
				_worldZip);
		}
	}

}
