package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServerMonitor implements Runnable {

	final String NETWORK_SPY_CHANNEL = "NetworkSpy";

	final Console _console;
	final Database _database;
	final ServerManager _manager;
	@SuppressWarnings("FieldCanBeLocal")
	final InetAddress _inetAddress;
	Map<String, ServerData> _servers;
	Map<String, ServerGroupData> _serverGroups;

	ServerMonitor(String[] args) throws UnknownHostException, FileNotFoundException {
		_console = System.console();
		_database = new Database();
		_manager = new ServerManager(this, new Scanner(new File("_path.dat")).nextLine());
		_inetAddress = InetAddress.getByName(args.length > 0 ? args[0] : "127.0.0.1");
		_servers = new HashMap<>();
		_serverGroups = new HashMap<>();

		new Thread(this).start();
	}

	static void main(String[] args) {
		try {
			new ServerMonitor(args);
		} catch (UnknownHostException | FileNotFoundException ex) {
			System.out.println("Exception while instantiating: " + String.join("\n", Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new)));
		}
	}

	public void log(String message) {
		_console.printf("\n[" + System.currentTimeMillis() + "] " + message);
		new Thread(() -> _database._jedis.publish(NETWORK_SPY_CHANNEL, message)).start();
	}

	boolean startServer(ServerGroupData group) {
		log(group._name + ": Starting new server...");
		ServerData serverData;
		try {
			serverData = _manager.startServer(_database._jedis, group);
		} catch (JedisException | MaxPortReachedException | IOException ex) {
			log(group._name + ": " + ex.getClass().getName() + " while starting new server: " + ex.getMessage());
			return false;
		}
		log(group._name + ": " + serverData._name + ": Started");
		return true;
	}

	boolean killServer(ServerData server, String reason) {
		log(server._group + ": " + server._name + ": Killing: " + reason);
		try {
			_manager.killServer(_database._jedis, server._name);
		} catch (JedisException | IOException ex) {
			log(server._group + ": " + server._name + ": " + ex.getClass().getName() + " while killing existing server: " + ex.getMessage());
			return false;
		}
		log(server._group + ": " + server._name + ": Killed: " + reason);
		return true;
	}

	void tick() throws JedisException {
		_servers = ServerQueries.getServersAsMap(_database._jedis);
		_serverGroups = ServerQueries.getServerGroupsAsMap(_database._jedis);

		for (ServerData serverData : _servers.values()) {
			ServerGroupData serverGroupData = _serverGroups.get(serverData._group);

			if (serverGroupData == null) {
				killServer(serverData, "Invalid Server Group");
				return;
			}

			if (serverData._port < serverGroupData._minPort || serverData._port > serverGroupData._maxPort) {
				killServer(serverData, "Port Outside Range");
				return;
			}

			List<String> motdStrings = Arrays.stream(serverData._motd.split(",")).toList();
			if (motdStrings.contains("DEAD")) {
				killServer(serverData, "Dead");
				return;
			}

			if ((System.currentTimeMillis() - serverData._updatedMillis) > serverGroupData._timeoutMillis) {
				killServer(serverData, "Unresponsive");
				return;
			}
		}

		for (ServerGroupData serverGroupData : _serverGroups.values().stream().sorted(Comparator.comparingInt(value -> value._minPort)).toArray(ServerGroupData[]::new)) {
			long totalServersAmount = _servers.values().stream().filter(serverData -> serverData._group.equals(serverGroupData._name)).count();
			long joinableServersAmount = _servers.values().stream().filter(serverData -> serverData._group.equals(serverGroupData._name)).filter(serverData -> !serverData._motd.startsWith("LIVE")).filter(serverData -> serverData._players < serverData._capacity).count();

			boolean isEnoughTotalServers = totalServersAmount >= serverGroupData._totalServers;
			boolean isOverflowTotalServers = totalServersAmount > serverGroupData._totalServers;

			boolean isEnoughJoinableServers = joinableServersAmount >= serverGroupData._joinableServers;
			boolean isOverflowJoinableServers = joinableServersAmount > serverGroupData._joinableServers;

			// Kill excess servers
			if (isOverflowTotalServers && isOverflowJoinableServers) {
				ServerData bestServerToKill = getBestServerToKill(_database._jedis, serverGroupData);
				if (bestServerToKill != null) {
					killServer(bestServerToKill, "Excess Servers");
					return;
				}
			}

			// Start minimum servers
			if (!isEnoughTotalServers || !isEnoughJoinableServers) {
				startServer(serverGroupData);
				return;
			}
		}

	}

	ServerData getBestServerToKill(UnifiedJedis jedis, ServerGroupData serverGroupData) {
		for (ServerData serverData : ServerQueries.getServers(jedis, serverGroupData)) {
			if (serverData._motd.startsWith("LIVE")) continue;
			if (serverData._players > (serverData._capacity / 3)) continue;
			return serverData;
		}
		return null;
	}

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			try {
				tick();
			} catch (JedisException ex) {
				log(ex.getClass().getName() + " while ticking: " + ex.getMessage());
			}

			try {
				//noinspection BusyWait
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}