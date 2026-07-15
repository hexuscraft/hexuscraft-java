package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

	final ServerMonitor _monitor;

	ServerManager(ServerMonitor serverMonitor) {
		_monitor = serverMonitor;
	}

	public ServerData startServer(UnifiedJedis jedis, ServerGroupData group)
		throws JedisException, MaxPortReachedException, IOException, InterruptedException {
		ServerData[] existingServers = ServerQueries.getServers(jedis, group);

		Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
		for (ServerData existingServer : existingServers) {
			serverDataIdMap.put(existingServer._port - group._minPort + 1, existingServer);
		}

		int lowestId = 0;
		for (int i = 1; i <= (group._maxPort - group._minPort + 1); i++) {
			if (serverDataIdMap.containsKey(i)) {
				continue;
			}
			lowestId = i;
			break;
		}

		if (lowestId == 0) throw new MaxPortReachedException();

		String id = group._id + "-" + lowestId;

		int serverPort = group._minPort + lowestId - 1;
		ServerData serverData = new ServerData(id,
			"",
			group._capacity,
			System.currentTimeMillis(),
			group._id,
			"",
			0,
			serverPort,
			20,
			System.currentTimeMillis(),
			true);
		serverData.update(jedis);

		Process process = new ProcessBuilder("scripts/startServer.cmd",
			serverData._id,
			serverData._group,
			Integer.toString(serverData._capacity),
			Integer.toString(serverData._port),
			Integer.toString(group._ramMB),
			group._plugin,
			Boolean.toString(group._worldEdit),
			group._worldZip).start();

		new Thread(() -> {
			try {
				_monitor.log(Arrays.toString(process.getInputStream().readAllBytes()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).start();

		int exitCode = process.waitFor();
		if (exitCode == 1) {
			_monitor.log("fail!");
			return null;
		}

		return serverData;
	}

	public void killServer(UnifiedJedis jedis, String id) throws JedisException, IOException {
		new ProcessBuilder("scripts/killServer.cmd", id).start();
		jedis.del(ServerQueries.SERVER(id));
	}

}
