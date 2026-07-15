package net.hexuscraft.common.database.data;

import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;

import java.util.UUID;

public class IServerGroupData {
	public String _id = null;

	public int _capacity = 20;
	public boolean _fallback = false;
	public GameType[] _games = new GameType[0];
	public UUID _host = null;
	public int _joinableServers = 0;
	public Integer _maxPort = null;
	public Integer _minPort = null;
	public String _plugin = null;
	public int _ramMB = 4096;
	public PermissionGroup[] _permissionGroups = new PermissionGroup[0];
	public int _totalServers = 0;
	public int _timeoutMillis = 30000;
	public boolean _worldEdit = false;
	public String _worldZip = "";
}
