package net.hexuscraft.database.serverdata;

import net.hexuscraft.database.queries.ServerQueries;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

public class ServerData {

    public final String _name;

    public final String _address;
    public final int _capacity;
    public final long _created;
    public final String _group;
    public final String _motd;
    public final int _players;
    public final int _port;
    public final double _tps;
    public final long _updated;

    public ServerData(final String name, final Map<String, String> serverData) {
        _name = name;

        _address = serverData.getOrDefault("address", "127.0.0.1");
        _capacity = Integer.parseInt(serverData.getOrDefault("capacity", "0"));
        _created = Long.parseLong(serverData.getOrDefault("created", "0"));
        _group = serverData.get("group");
        _motd = serverData.getOrDefault("motd", "");
        _players = Integer.parseInt(serverData.getOrDefault("players", "0"));
        _port = Integer.parseInt(serverData.getOrDefault("port", "0"));
        _tps = Double.parseDouble(serverData.getOrDefault("tps", "20"));
        _updated = Long.parseLong(serverData.getOrDefault("updated", "0"));
    }

    public ServerData(final String name, final String address, final int capacity, final long created,
                      final String group, final String motd, final int players, final int port,
                      final double tps, final long updated) {
        _name = name;

        _address = address;
        _capacity = capacity;
        _created = created;
        _group = group;
        _motd = motd;
        _players = players;
        _port = port;
        _tps = tps;
        _updated = updated;
    }

    public final Map<String, String> toMap() {
        return Map.of(
                "address", _address,
                "capacity", Integer.toString(_capacity),
                "created", Long.toString(_created),
                "group", _group,
                "motd", _motd,
                "players", Integer.toString(_players),
                "port", Integer.toString(_port),
                "tps", Double.toString(_tps),
                "updated", Long.toString(_updated)
        );
    }

    public final void update(final JedisPooled jedis) {
        jedis.hset(ServerQueries.SERVER(_name), toMap());
    }

}
