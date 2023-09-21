package net.hexuscraft.database.serverdata;

import net.hexuscraft.database.queries.ServerQueries;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

public class ServerData {

    public final String _name;

    public final String _group;
    public final long _created;
    public final long _updated;
    public final String _address;
    public final int _port;
    public final int _players;
    public final int _capacity;
    public final String _motd;
    public final double _tps;

    public ServerData(final String name, final Map<String, String> serverData) {
        _name = name;

        _group = serverData.get("group");
        _created = Long.parseLong(serverData.getOrDefault("created", "0"));
        _updated = Long.parseLong(serverData.getOrDefault("updated", "0"));
        _address = serverData.getOrDefault("address", "127.0.0.1");
        _port = Integer.parseInt(serverData.getOrDefault("port", "0"));
        _players = Integer.parseInt(serverData.getOrDefault("players", "0"));
        _capacity = Integer.parseInt(serverData.getOrDefault("capacity", "0"));
        _motd = serverData.getOrDefault("motd", "");
        _tps = Double.parseDouble(serverData.getOrDefault("tps", "20"));
    }

    public ServerData(final String name, final String group, final long created, final long updated,
                      final String address, final int port, final int players, final int capacity, final String motd,
                      final double tps) {
        _name = name;

        _group = group;
        _created = created;
        _updated = updated;
        _address = address;
        _port = port;
        _players = players;
        _capacity = capacity;
        _motd = motd;
        _tps = tps;
    }

    public final Map<String, String> toMap() {
        return Map.of(
                "group", _group,
                "created", Long.toString(_created),
                "updated", Long.toString(_updated),
                "address", _address,
                "port", Integer.toString(_port),
                "players", Integer.toString(_players),
                "capacity", Integer.toString(_capacity),
                "motd", _motd,
                "tps", Double.toString(_tps)
        );
    }

    public final void update(final JedisPooled jedis) {
        jedis.hset(ServerQueries.SERVER(_name), toMap());
    }

}
