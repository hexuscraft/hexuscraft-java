package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.UtilUniqueId;
import redis.clients.jedis.UnifiedJedis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerGroupData
{

    public final String _name;

    public final int _capacity;
    public final GameType[] _games;
    public final UUID _hostUniqueId;
    public final int _joinableServers;
    public final int _maxPort;
    public final int _minPort;
    public final String _plugin;
    public final int _ram;
    public final PermissionGroup _requiredPermission;
    public final int _totalServers;
    public final int _timeoutMillis;
    public final boolean _worldEdit;
    public final String _worldZip;

    public ServerGroupData(final String name, final Map<String, String> serverGroupData)
    {
        _name = name;

        _requiredPermission = PermissionGroup.valueOf(serverGroupData.getOrDefault("requiredPermission",
                                                                                   PermissionGroup._PLAYER.name()));
        _minPort = Integer.parseInt(serverGroupData.getOrDefault("minPort", "0"));
        _maxPort = Integer.parseInt(serverGroupData.getOrDefault("maxPort", "0"));
        _totalServers = Integer.parseInt(serverGroupData.getOrDefault("totalServers", "0"));
        _joinableServers = Integer.parseInt(serverGroupData.getOrDefault("joinableServers", "0"));
        _plugin = serverGroupData.getOrDefault("plugin", "");
        _worldZip = serverGroupData.getOrDefault("worldZip", "");
        _ram = Integer.parseInt(serverGroupData.getOrDefault("ram", "512"));
        _capacity = Integer.parseInt(serverGroupData.getOrDefault("capacity", "20"));
        _worldEdit = Boolean.parseBoolean(serverGroupData.getOrDefault("worldEdit", "FALSE"));
        _timeoutMillis = Integer.parseInt(serverGroupData.getOrDefault("timeoutMillis", "30000"));
        _games = Arrays.stream(serverGroupData.getOrDefault("game", "").split(","))
                       .filter(s -> !s.trim().isEmpty())
                       .map(GameType::valueOf)
                       .toArray(GameType[]::new);
        _hostUniqueId = UUID.fromString(serverGroupData.getOrDefault("hostUniqueId",
                                                                     UtilUniqueId.EMPTY_UUID.toString()));
    }

    public ServerGroupData(final String name,
                           final PermissionGroup requiredPermission,
                           final int minPort,
                           final int maxPort,
                           final int totalServers,
                           final int joinableServers,
                           final String plugin,
                           final String worldZip,
                           final int ram,
                           final int capacity,
                           final boolean worldEdit,
                           final int timeoutMillis,
                           final GameType[] games,
                           final UUID hostUniqueId)
    {
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
        _hostUniqueId = (hostUniqueId == null) ? UtilUniqueId.EMPTY_UUID : hostUniqueId;
    }

    public Map<String, String> toMap()
    {
        final Map<String, String> map = new HashMap<>();
        map.put("requiredPermission", _requiredPermission.name());
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
        map.put("game", String.join(",", Arrays.stream(_games).map(GameType::name).toArray(String[]::new)));
        map.put("hostUniqueId", _hostUniqueId.toString());
        return map;
    }

    public void update(final UnifiedJedis jedis)
    {
        jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
    }

    public ServerData[] getServers(final UnifiedJedis jedis)
    {
        return ServerQueries.getServers(jedis, this);
    }

}
