package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;

public class ServerData
{

    public String _name;

    public String _address;
    public int _capacity;
    public long _createdMillis;
    public String _group;
    public String _motd;
    public int _players;
    public int _port;
    public double _tps;
    public long _updated;
    public boolean _updatedByMonitor;

    public ServerData(String name, Map<String, String> serverData)
    {
        _name = name;

        _address = serverData.getOrDefault("address", "127.0.0.1");
        _capacity = Integer.parseInt(serverData.getOrDefault("capacity", "0"));
        _createdMillis = Long.parseLong(serverData.getOrDefault("createdMillis", "0"));
        _group = serverData.get("group");
        _motd = serverData.getOrDefault("motd", "");
        _players = Integer.parseInt(serverData.getOrDefault("players", "0"));
        _port = Integer.parseInt(serverData.getOrDefault("port", "0"));
        _tps = Double.parseDouble(serverData.getOrDefault("tps", "20"));
        _updated = Long.parseLong(serverData.getOrDefault("updated", "0"));
        _updatedByMonitor = Boolean.parseBoolean(serverData.getOrDefault("updatedByMonitor", "false"));
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
                      long updated,
                      boolean updatedByMonitor)
    {
        _name = name;

        _address = address;
        _capacity = capacity;
        _createdMillis = createdMillis;
        _group = group;
        _motd = motd;
        _players = players;
        _port = port;
        _tps = tps;
        _updated = updated;
        _updatedByMonitor = updatedByMonitor;
    }

    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("address", _address);
        map.put("capacity", Integer.toString(_capacity));
        map.put("createdMillis", Long.toString(_createdMillis));
        map.put("group", _group);
        map.put("motd", _motd);
        map.put("players", Integer.toString(_players));
        map.put("port", Integer.toString(_port));
        map.put("tps", Double.toString(_tps));
        map.put("updated", Long.toString(_updated));
        map.put("updatedByMonitor", Boolean.toString(_updatedByMonitor));
        return map;
    }

    public void update(UnifiedJedis jedis)
    {
        jedis.hset(ServerQueries.SERVER(_name), toMap());
    }

}
