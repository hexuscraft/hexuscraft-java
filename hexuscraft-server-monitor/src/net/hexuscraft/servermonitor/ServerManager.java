package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

	final ServerMonitor _monitor;
	final String _path;

	ServerManager(ServerMonitor serverMonitor, String path) {
		_monitor = serverMonitor;
		_path = path;
	}

	public ServerData startServer(UnifiedJedis jedis, ServerGroupData serverGroupData) throws JedisException, MaxPortReachedException, IOException {
		ServerData[] existingServers = ServerQueries.getServers(jedis, serverGroupData);

		Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
		for (ServerData existingServer : existingServers) {
			serverDataIdMap.put(existingServer._port - serverGroupData._minPort + 1, existingServer);
		}

		int lowestId = 0;
		for (int i = 1; i <= (serverGroupData._maxPort - serverGroupData._minPort + 1); i++) {
			if (serverDataIdMap.containsKey(i)) {
				continue;
			}
			lowestId = i;
			break;
		}

		if (lowestId == 0) throw new MaxPortReachedException();

		String serverName = serverGroupData._name + "-" + lowestId;

		int serverPort = serverGroupData._minPort + lowestId - 1;
		ServerData serverData = new ServerData(serverName, "", serverGroupData._capacity, System.currentTimeMillis(), serverGroupData._name, "", 0, serverPort, 20, System.currentTimeMillis(), true);
		serverData.update(jedis);

		new ProcessBuilder(_path + "/Scripts/startServer.cmd", serverData._name, serverData._group, Integer.toString(serverData._port), Integer.toString(serverGroupData._ram), Integer.toString(serverData._capacity), serverGroupData._plugin, serverGroupData._worldZip, Boolean.toString(serverGroupData._worldEdit), Boolean.toString(serverGroupData._viaVersion)).start();
		return serverData;
	}

	public void killServer(UnifiedJedis jedis, String serverName) throws JedisException, IOException {
		new ProcessBuilder(_path + "/Scripts/killServer.cmd", serverName).start();
		jedis.del(ServerQueries.SERVER(serverName));
	}

}
