package net.hexuscraft.database.serverdata;

import java.util.Map;
import java.util.UUID;

public class ServerData {

    public final UUID _uuid;
    public final String _name;
    public final UUID _group;
    public final int _maxPlayers;
    public final long _lastUpdate;
    public final String _serverIp;
    public final int _serverPort;
    public final int _playerCount;
    public final UUID _host;

    public ServerData(UUID uuid, Map<String, String> serverData) {
        _uuid = uuid;
        _name = serverData.get("name");
        _group = UUID.fromString(serverData.get("group"));
        _maxPlayers = Integer.parseInt(serverData.getOrDefault("maxPlayers", "0"));
        _lastUpdate = Long.parseLong(serverData.getOrDefault("lastUpdate", "0"));
        _serverIp = serverData.getOrDefault("serverIp", "127.0.0.1");
        _serverPort = Integer.parseInt(serverData.getOrDefault("serverPort", "0"));
        _playerCount = Integer.parseInt(serverData.getOrDefault("playerCount", "0"));
        _host = serverData.containsKey("host") ? UUID.fromString(serverData.get("host")) : null;
    }

}
