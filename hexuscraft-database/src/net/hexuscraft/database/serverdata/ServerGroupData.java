package net.hexuscraft.database.serverdata;

import net.hexuscraft.database.queries.ServerQueries;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

public class ServerGroupData {

    public final String _name;

    public final String _prefix;
    public final String _requiredPermission;
    public final int _minPort;
    public final int _maxPort;
    public final int _totalServers;
    public final int _joinableServers;
    public final String _plugin;
    public final String _worldZip;
    public final int _ram;

    public ServerGroupData(final String name, final Map<String, String> serverGroupData) {
        _name = name;

        _prefix = serverGroupData.get("prefix");
        _requiredPermission = serverGroupData.get("requiredPermission");
        _minPort = Integer.parseInt(serverGroupData.getOrDefault("minPort", "0"));
        _maxPort = Integer.parseInt(serverGroupData.getOrDefault("maxPort", "0"));
        _totalServers = Integer.parseInt(serverGroupData.getOrDefault("totalServers", "0"));
        _joinableServers = Integer.parseInt(serverGroupData.getOrDefault("joinableServers", "0"));
        _plugin = serverGroupData.getOrDefault("plugin", "");
        _worldZip = serverGroupData.getOrDefault("worldZip", "");
        _ram = Integer.parseInt(serverGroupData.getOrDefault("ram", "512"));
    }

    public ServerGroupData(final String name, final String prefix, final String requiredPermission,
                           final int minPort, final int maxPort, final int totalServers, final int joinableServers,
                           final String plugin, final String worldZip, final int ram) {
        _name = name;

        _prefix = prefix;
        _requiredPermission = requiredPermission;
        _minPort = minPort;
        _maxPort = maxPort;
        _totalServers = totalServers;
        _joinableServers = joinableServers;
        _plugin = plugin;
        _worldZip = worldZip;
        _ram = ram;
    }

    public final Map<String, String> toMap() {
        return Map.of(
                "prefix", _prefix,
                "requiredPermission", _requiredPermission,
                "minPort", Integer.toString(_minPort),
                "maxPort", Integer.toString(_maxPort),
                "totalServers", Integer.toString(_totalServers),
                "joinableServers", Integer.toString(_joinableServers),
                "plugin", _plugin,
                "worldZip", _worldZip,
                "ram", Integer.toString(_ram)
        );
    }

    public final void update(final JedisPooled jedis) {
        jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
    }

}
