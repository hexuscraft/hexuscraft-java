package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;

import java.util.Map;

public class ServerData extends IServerData {

	public ServerData(Map<String, String> serverData) {
		assert serverData.containsKey("name");
		_name = serverData.get("name");

		assert serverData.containsKey("address");
		_address = serverData.get("address");

		if (serverData.containsKey("capacity")) {
			_capacity = Integer.parseInt(serverData.get("capacity"));
		}

		if (serverData.containsKey("createdMillis")) {
			_createdMillis = Long.parseLong(serverData.get("createdMillis"));
		}

		assert serverData.containsKey("group");
		_group = serverData.get("group");

		if (serverData.containsKey("motd")) {
			_motd = serverData.get("motd");
		}

		if (serverData.containsKey("players")) {
			_players = Integer.parseInt(serverData.get("players"));
		}

		assert serverData.containsKey("port");
		_port = Integer.parseInt(serverData.get("port"));

		if (serverData.containsKey("tps")) {
			_tps = Double.parseDouble(serverData.get("tps"));
		}

		if (serverData.containsKey("updatedMillis")) {
			_updatedMillis = Long.parseLong(serverData.get("updatedMillis"));
		}

		if (serverData.containsKey("updatedByMonitor")) {
			_updatedByMonitor = Boolean.parseBoolean(serverData.get("updatedByMonitor"));
		}
	}

	public ServerData(String name,
	                  String address,
	                  int capacity,
	                  long createdMillis,
	                  String group,
	                  String motd,
	                  int players,
	                  int port,
	                  double tps,
	                  long updatedMillis,
	                  boolean updatedByMonitor) {
		_name = name;
		_address = address;
		_capacity = capacity;
		_createdMillis = createdMillis;
		_group = group;
		_motd = motd;
		_players = players;
		_port = port;
		_tps = tps;
		_updatedMillis = updatedMillis;
		_updatedByMonitor = updatedByMonitor;
	}

	public Map<String, String> toMap() {
		return Map.ofEntries(Map.entry("name", _name),
			Map.entry("address", _address),
			Map.entry("capacity", Integer.toString(_capacity)),
			Map.entry("createdMillis", Long.toString(_createdMillis)),
			Map.entry("group", _group),
			Map.entry("motd", _motd),
			Map.entry("players", Integer.toString(_players)),
			Map.entry("port", Integer.toString(_port)),
			Map.entry("tps", Double.toString(_tps)),
			Map.entry("updatedMillis", Long.toString(_updatedMillis)),
			Map.entry("updatedByMonitor", Boolean.toString(_updatedByMonitor)));
	}

	public void update(UnifiedJedis jedis) {
		jedis.hset(ServerQueries.SERVER(_name), toMap());
	}

	public void delete(UnifiedJedis jedis) {
		jedis.del(ServerQueries.SERVER(_name));
	}

	public static class Builder extends IServerData {

		public Builder name(String value) {
			_name = value;
			return this;
		}

		public Builder address(String value) {
			_address = value;
			return this;
		}

		public Builder capacity(int value) {
			_capacity = value;
			return this;
		}

		public Builder createdMillis(long value) {
			_createdMillis = value;
			return this;
		}

		public Builder group(String value) {
			_group = value;
			return this;
		}

		public Builder motd(String value) {
			_motd = value;
			return this;
		}

		public Builder players(int value) {
			_players = value;
			return this;
		}

		public Builder port(int value) {
			_port = value;
			return this;
		}

		public Builder tps(double value) {
			_tps = value;
			return this;
		}

		public Builder updatedMillis(long value) {
			_updatedMillis = value;
			return this;
		}

		public Builder updatedByMonitor(boolean value) {
			_updatedByMonitor = value;
			return this;
		}

		public ServerData build() {
			return new ServerData(_name,
				_address,
				_capacity,
				_createdMillis,
				_group,
				_motd,
				_players,
				_port,
				_tps,
				_updatedMillis,
				_updatedByMonitor);
		}
	}

}
