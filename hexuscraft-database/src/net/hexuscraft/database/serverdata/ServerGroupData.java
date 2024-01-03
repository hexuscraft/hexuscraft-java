package net.hexuscraft.database.serverdata;

import net.hexuscraft.database.queries.ServerQueries;
import redis.clients.jedis.JedisPooled;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerGroupData {

    public final String _name;

    public final String _requiredPermission;
    public final int _minPort;
    public final int _maxPort;
    public final int _totalServers;
    public final int _joinableServers;
    public final String _plugin;
    public final String _worldZip;
    public final int _ram;
    public final int _capacity;

    public ServerGroupData(final String name, final Map<String, String> serverGroupData) {
        _name = name;

        _requiredPermission = serverGroupData.getOrDefault("requiredPermission", "MEMBER");
        _minPort = Integer.parseInt(serverGroupData.getOrDefault("minPort", "0"));
        _maxPort = Integer.parseInt(serverGroupData.getOrDefault("maxPort", "0"));
        _totalServers = Integer.parseInt(serverGroupData.getOrDefault("totalServers", "0"));
        _joinableServers = Integer.parseInt(serverGroupData.getOrDefault("joinableServers", "0"));
        _plugin = serverGroupData.getOrDefault("plugin", "");
        _worldZip = serverGroupData.getOrDefault("worldZip", "");
        _ram = Integer.parseInt(serverGroupData.getOrDefault("ram", "512"));
        _capacity = Integer.parseInt(serverGroupData.getOrDefault("capacity", "20"));
    }

    public ServerGroupData(final String name, final String requiredPermission,
                           final int minPort, final int maxPort, final int totalServers, final int joinableServers,
                           final String plugin, final String worldZip, final int ram, final int capacity) {
        _name = name;

        _requiredPermission = requiredPermission;
        _minPort = minPort;
        _maxPort = maxPort;
        _totalServers = totalServers;
        _joinableServers = joinableServers;
        _plugin = plugin;
        _worldZip = worldZip;
        _ram = ram;
        _capacity = capacity;
    }

    public final Map<String, String> toMap() {
        return new HashMap<>(Map.of(
                "requiredPermission", _requiredPermission,
                "minPort", Integer.toString(_minPort),
                "maxPort", Integer.toString(_maxPort),
                "totalServers", Integer.toString(_totalServers),
                "joinableServers", Integer.toString(_joinableServers),
                "plugin", _plugin,
                "worldZip", _worldZip,
                "ram", Integer.toString(_ram),
                "capacity", Integer.toString(_capacity)
        ));
    }

    public final void update(final JedisPooled jedis) {
        jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
    }

}
