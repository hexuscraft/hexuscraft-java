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

public class ServerGroupData
{

    public String _name;

    public int _capacity;
    public GameType[] _games;
    public UUID _hostUniqueId;
    public int _joinableServers;
    public int _maxPort;
    public int _minPort;
    public String _plugin;
    public int _ram;
    public PermissionGroup _requiredPermission;
    public int _totalServers;
    public int _timeoutMillis;
    public boolean _worldEdit;
    public String _worldZip;

    public ServerGroupData(String name, Map<String, String> serverGroupData)
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

    public ServerGroupData(String name,
                           PermissionGroup requiredPermission,
                           int minPort,
                           int maxPort,
                           int totalServers,
                           int joinableServers,
                           String plugin,
                           String worldZip,
                           int ram,
                           int capacity,
                           boolean worldEdit,
                           int timeoutMillis,
                           GameType[] games,
                           UUID hostUniqueId)
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
        Map<String, String> map = new HashMap<>();
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

    public void update(UnifiedJedis jedis)
    {
        jedis.hset(ServerQueries.SERVERGROUP(_name), toMap());
    }

    public ServerData[] getServers(UnifiedJedis jedis)
    {
        return ServerQueries.getServers(jedis, this);
    }

}
