package net.hexuscraft.database.serverdata;

import java.util.Map;

public class ServerGroupData {

    public final String _prefix;
    public final String _requiredPermission;
    public final int _minPort;
    public final long _maxPort;
    public final ServerGroupType _type;

    public ServerGroupData(Map<String, String> serverGroupData) {
        _prefix = serverGroupData.get("prefix");
        _requiredPermission = serverGroupData.get("requiredPermission");
        _minPort = Integer.parseInt(serverGroupData.getOrDefault("minPort", "0"));
        _maxPort = Integer.parseInt(serverGroupData.getOrDefault("maxPort", "0"));
        _type = ServerGroupType.valueOf(serverGroupData.get("type"));
    }

}
