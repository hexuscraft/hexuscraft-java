package net.hexuscraft.common.database.data;

public class IServerData {
    public String _name = "";
    public String _address = "127.0.0.1";
    public int _capacity = 0;
    public long _createdMillis = 0;
    public String _group = "";
    public String _motd = "";
    public int _players = 20;
    public int _port = 0;
    public double _tps = 20;
    public long _updatedMillis = System.currentTimeMillis();
    public boolean _updatedByMonitor = false;
}
