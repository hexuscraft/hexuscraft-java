package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import redis.clients.jedis.UnifiedJedis;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerGroupData extends IServerGroupData {

	public ServerGroupData(String id, Map<String, String> serverGroupData) {
		_id = Objects.requireNonNull(id, "id");

		if (serverGroupData.containsKey("capacity"))
			_capacity = Integer.parseInt(serverGroupData.get("capacity"));

		if (serverGroupData.containsKey("fallback"))
			_fallback = Boolean.parseBoolean(serverGroupData.get("fallback"));

		if (serverGroupData.containsKey("games")) _games =
			Arrays.stream(serverGroupData.get("games").split(","))
				.filter(s -> !s.trim().isEmpty())
				.map(GameType::valueOf)
				.toArray(GameType[]::new);

		if (serverGroupData.containsKey("host")) _host = UUID.fromString(serverGroupData.get("host"));

		if (serverGroupData.containsKey("joinableServers"))
			_joinableServers = Integer.parseInt(serverGroupData.get("joinableServers"));

		_maxPort = Integer.parseInt(Objects.requireNonNull(serverGroupData.get("maxPort"), "maxPort"));

		_minPort = Integer.parseInt(Objects.requireNonNull(serverGroupData.get("minPort"), "minPort"));

		_plugin = Objects.requireNonNull(serverGroupData.get("plugin"), "plugin");

		if (serverGroupData.containsKey("ram"))
			_ramMB = Integer.parseInt(serverGroupData.get("ram"));

		if (serverGroupData.containsKey("permissionGroups"))
			_permissionGroups = Arrays.stream(serverGroupData.get("permissionGroups").split(","))
				.filter(s -> !s.trim().isEmpty())
				.map(PermissionGroup::valueOf)
				.toArray(PermissionGroup[]::new);

		if (serverGroupData.containsKey("totalServers"))
			_totalServers = Integer.parseInt(serverGroupData.get("totalServers"));

		if (serverGroupData.containsKey("timeoutMillis"))
			_timeoutMillis = Integer.parseInt(serverGroupData.get("timeoutMillis"));

		if (serverGroupData.containsKey("worldEdit"))
			_worldEdit = Boolean.parseBoolean(serverGroupData.get("worldEdit"));

		if (serverGroupData.containsKey("worldZip"))
			_worldZip = serverGroupData.get("worldZip");
	}

	public ServerGroupData(String id,

			       int capacity,
			       boolean fallback,
			       GameType[] games,
			       UUID hostUUID,
			       int joinableServers,
			       int maxPort,
			       int minPort,
			       String plugin,
			       int ramMB,
			       PermissionGroup[] permissionGroups,
			       int totalServers,
			       int timeoutMillis,
			       boolean worldEdit,
			       String worldZip) {
		_id = id;

		_capacity = capacity;
		_fallback = fallback;
		_games = games;
		_host = hostUUID;
		_joinableServers = joinableServers;
		_maxPort = maxPort;
		_minPort = minPort;
		_plugin = plugin;
		_ramMB = ramMB;
		_permissionGroups = permissionGroups;
		_totalServers = totalServers;
		_timeoutMillis = timeoutMillis;
		_worldEdit = worldEdit;
		_worldZip = worldZip;
	}

	public Map<String, String> toMap() {
		return Map.ofEntries(Map.entry("capacity", Integer.toString(_capacity)),
			Map.entry("fallback", Boolean.toString(_fallback)),
			Map.entry("games",
				String.join(",", Arrays.stream(_games).map(GameType::name).toArray(String[]::new))),
			Map.entry("host", _host.toString()),
			Map.entry("joinableServers", Integer.toString(_joinableServers)),
			Map.entry("maxPort", Integer.toString(_maxPort)),
			Map.entry("minPort", Integer.toString(_minPort)),
			Map.entry("plugin", _plugin),
			Map.entry("ramMB", Integer.toString(_ramMB)),
			Map.entry("permissionGroups",
				String.join(",",
					Arrays.stream(_permissionGroups)
						.map(PermissionGroup::name)
						.toArray(String[]::new))),
			Map.entry("totalServers", Integer.toString(_totalServers)),
			Map.entry("timeoutMillis", Integer.toString(_timeoutMillis)),
			Map.entry("worldEdit", Boolean.toString(_worldEdit)),
			Map.entry("worldZip", _worldZip));
	}

	public void update(UnifiedJedis jedis) {
		jedis.hset(ServerQueries.SERVERGROUP(_id), toMap());
	}

	public ServerData[] getServers(UnifiedJedis jedis) {
		return ServerQueries.getServers(jedis, this);
	}

	public static class Builder extends IServerGroupData {
		public Builder id(String value) {
			_id = value;
			return this;
		}

		public Builder capacity(int value) {
			_capacity = value;
			return this;
		}

		public Builder fallback(boolean value) {
			_fallback = value;
			return this;
		}

		public Builder games(GameType... value) {
			_games = value;
			return this;
		}

		public Builder host(UUID value) {
			_host = value;
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
			_ramMB = value;
			return this;
		}

		public Builder permissionGroups(PermissionGroup... value) {
			_permissionGroups = value;
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

		public Builder worldEdit(boolean value) {
			_worldEdit = value;
			return this;
		}

		public Builder worldZip(String value) {
			_worldZip = value;
			return this;
		}

		public ServerGroupData build() {
			return new ServerGroupData(_id,

				_capacity,
				_fallback,
				_games,
				_host,
				_joinableServers,
				_maxPort,
				_minPort,
				_plugin,
				_ramMB,
				_permissionGroups,
				_totalServers,
				_timeoutMillis,
				_worldEdit,
				_worldZip);
		}
	}

}
