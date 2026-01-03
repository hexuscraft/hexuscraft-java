package net.hexuscraft.common.database.serverdata;

import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerGroupData {

    public final String _name;

    public final int _capacity;
    public final String[] _games;
    public final UUID _hostUUID;
    public final int _joinableServers;
    public final int _maxPort;
    public final int _minPort;
    public final String _plugin;
    public final int _ram;
    public final String _requiredPermission;
    public final int _totalServers;
    public final int _timeoutMillis;
    public final boolean _worldEdit;
    public final String _worldZip;

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
        _worldEdit = Boolean.parseBoolean(serverGroupData.getOrDefault("worldEdit", "FALSE"));
        _timeoutMillis = Integer.parseInt(serverGroupData.getOrDefault("timeoutMillis", "10000"));
        _games = serverGroupData.getOrDefault("games", "").split(",");
        _hostUUID = UUID.fromString(serverGroupData.getOrDefault("hostUUID", "00000000-0000-0000-0000-000000000000"));
    }

    public ServerGroupData(final String name, final String requiredPermission,
                           final int minPort, final int maxPort, final int totalServers, final int joinableServers,
                           final String plugin, final String worldZip, final int ram, final int capacity,
                           final boolean worldEdit, final int timeoutMillis, final String[] games,
                           final UUID hostUUID) {
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
        _worldEdit = worldEdit;
        _timeoutMillis = timeoutMillis;
        _games = games;
        _hostUUID = (hostUUID == null) ? UUID.fromString("00000000-0000-0000-0000-000000000000") : hostUUID;
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("requiredPermission", _requiredPermission);
        map.put("minPort", Integer.toString(_minPort));
        map.put("maxPort", Integer.toString(_maxPort));
        map.put("totalServers", Integer.toString(_totalServers));
        map.put("joinableServers", Integer.toString(_joinableServers));
        map.put("plugin", _plugin);
        map.put("worldZip", _worldZip);
        map.put("ram", Integer.toString(_ram));
        map.put("capacity", Integer.toString(_capacity));
        map.put("worldEdit", Boolean.toString(_worldEdit));
        map.put("timeoutMillis", Integer.toString(_timeoutMillis));
        map.put("games", String.join(",", _games));
        map.put("hostUUID", _hostUUID.toString());
        return map;
    }

    public void update(final UnifiedJedis jedis) {
        jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
    }

    public ServerData[] getServers(final UnifiedJedis jedis) {
        return ServerQueries.getServers(jedis, this);
    }

}
