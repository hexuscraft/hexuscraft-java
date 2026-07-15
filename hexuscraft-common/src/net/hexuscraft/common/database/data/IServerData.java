package net.hexuscraft.common.database.data;

public class IServerData {
	public String _id = null;

	public String _address = null;
	public int _capacity = 0;
	public long _createdMillis = 0;
	public String _group = null;
	public String _motd = "";
	public int _players = 20;
	public Integer _port = null;
	public double _tps = 20;
	public long _updatedMillis = System.currentTimeMillis();
	public boolean _updatedByMonitor = false;
}
