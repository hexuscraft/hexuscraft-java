package net.hexuscraft.common.database.data;

import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;

import java.util.UUID;

public class IServerGroupData {
	public String _name = "";
	public int _capacity = 20;
	public GameType[] _games = new GameType[0];
	public UUID _hostUUID = null;
	public int _joinableServers = 0;
	public int _maxPort = 0;
	public int _minPort = 0;
	public String _plugin = "";
	public int _ram = 2048;
	public PermissionGroup _requiredPermission = PermissionGroup._PLAYER;
	public int _totalServers = 0;
	public int _timeoutMillis = 30000;
	public boolean _viaVersion = true;
	public boolean _worldEdit = false;
	public String _worldZip = "";
}
