package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.hexuscraft.servermonitor.database.PluginDatabase;
import redis.clients.jedis.JedisPooled;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerMonitor implements Runnable {

    public static void main(String[] args) {
        new ServerMonitor();
    }

    private final long NATURAL_TIMEOUT_MS = 100L;
    private final long MAX_SERVER_IDLE_MS = 10000L;

    private final Console _console;
    private final PluginDatabase _database;

    private final Map<String, ServerData> _serverDataMap;
    private final Map<String, ServerGroupData> _serverGroupDataMap;

    private ServerMonitor() {
        _console = System.console();
        _database = new PluginDatabase();

        _serverDataMap = new HashMap<>();
        _serverGroupDataMap = new HashMap<>();

        new Thread(this).start();
    }

    private void log(final String message, final Object... args) {
        _console.printf("[" + System.currentTimeMillis() + "] " + message + "\n", args);
    }

    private void tick() {
        final JedisPooled jedis = _database.getJedisPooled();

        _serverDataMap.clear();
        _serverDataMap.putAll(ServerQueries.getServersAsMap(jedis));

        _serverGroupDataMap.clear();
        _serverGroupDataMap.putAll(ServerQueries.getServerGroupsAsMap(jedis));

        final Map<ServerGroupData, Set<ServerData>> totalServersMap = new HashMap<>();
        final Map<ServerGroupData, Set<ServerData>> joinableServersMap = new HashMap<>();

        _serverGroupDataMap.forEach((serverGroupName, serverGroupData) -> {
            totalServersMap.put(serverGroupData, new HashSet<>());
            joinableServersMap.put(serverGroupData, new HashSet<>());
        });

        _serverDataMap.forEach((serverName, serverData) -> {

            // Kill dead servers
            if ((System.currentTimeMillis() - serverData._updated) > MAX_SERVER_IDLE_MS) {
                killServer(jedis, serverData, "Dead");
                return;
            }

            // Kill pending death servers
            final String[] nameArray = serverData._name.split("-",3);
            if (nameArray.length > 3 && nameArray[2].equals("KILL")) {
                killServer(jedis, serverData, "Pending");
                return;
            }

            // Count total and joinable servers
            final ServerGroupData serverGroupData = _serverGroupDataMap.get(serverData._group);
            totalServersMap.get(serverGroupData).add(serverData);
            if (serverData._players < serverData._capacity) {
                joinableServersMap.get(serverGroupData).add(serverData);
            }

        });

        _serverGroupDataMap.forEach((serverGroupName, serverGroupData) -> {

            final Set<ServerData> totalServers = totalServersMap.get(serverGroupData);
            final int totalServersAmount = totalServers.size();
            final Set<ServerData> joinableServers = joinableServersMap.get(serverGroupData);
            final int joinableServersAmount = joinableServers.size();

            final boolean isEnoughTotalServers = totalServersAmount >= serverGroupData._totalServers;
            final boolean isOverflowTotalServers = totalServersAmount > serverGroupData._totalServers;

            final boolean isEnoughJoinableServers = joinableServersAmount >= serverGroupData._joinableServers;
            final boolean isOverflowJoinableServers = joinableServersAmount > serverGroupData._joinableServers;

            // Start minimum servers
            if (!isEnoughTotalServers) {
                startServer(jedis, serverGroupData, "Insufficient Total Servers");
            }

            // Kill excess servers
            if (isOverflowTotalServers && !isOverflowJoinableServers) {

            }

        });
    }

    private void killServer(final JedisPooled jedis, final String serverName, final String reason) {
        log("=== KILL SERVER ===");
        log("Reason: " + reason);
        log("Name: " + serverName);

        try {
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "start",
                    "C:/minecraft/scripts/killServer.cmd",
                    "127.0.0.1",
                    "193.110.160.24",
                    serverName
            ).start();

            jedis.del(ServerQueries.SERVER(serverName));
        } catch (IOException e) {
            log("!!! Exception while running kill process:");
            log(e.getMessage(), e);
        }

        log("====================");
    }

    private void killServer(final JedisPooled jedis, final ServerData serverData, final String reason) {
        killServer(jedis, serverData._name, reason);
    }

    private void getBestServerToKill(final JedisPooled jedis, final ServerGroupData serverGroupData) {
        int selectedServerPlayers = 0;

    }

    private void startServer(final JedisPooled jedis, final ServerGroupData serverGroupData, final String reason) {
        log("=== START SERVER ===");
        log("Reason: " + reason);
        log("Group: " + serverGroupData._name);

        final ServerData[] existingServers = ServerQueries.getServers(jedis, serverGroupData);

        final Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
        for (ServerData existingServer : existingServers) {
            serverDataIdMap.put(Integer.parseInt(existingServer._name.split("-")[1]), existingServer);
        }

        //noinspection ReassignedVariable
        int lowestId = 0;
        // we +1 as the normal server port is _minPort + id, and there cannot be a server with id 0.
        for (int i = 1; i < (serverGroupData._maxPort - serverGroupData._minPort + 1); i++) {
            if (serverDataIdMap.containsKey(i)) continue;
            lowestId = i;
            break;
        }

        if (lowestId == 0) {
            log("!!! There are no server spaces available within this group's port range. Cancelling.");
            log("====================");
            return;
        }

        final String serverName = serverGroupData._prefix + "-" + lowestId;

        log("Ram: " + serverGroupData._ram);
        log("Name: " + serverName);

        try {
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "start",
                    "C:/minecraft/scripts/startServer.cmd",
                    "127.0.0.1",
                    "193.110.160.24",
                    Integer.toString(serverGroupData._minPort + lowestId),
                    Integer.toString(serverGroupData._ram),
                    serverGroupData._worldZip,
                    serverGroupData._plugin,
                    serverGroupData._name,
                    serverGroupData._prefix + "-" + lowestId,
                    "false"
            ).start();
            log("Waiting for server to startup...");

            long startMs = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - startMs) > 20000L) {
                    killServer(jedis, serverName, "Slow Start-up");
                    break;
                }
                if (ServerQueries.getServer(jedis, serverName) == null) {
                    //noinspection BusyWait
                    Thread.sleep(100L);
                    continue;
                }
                break;
            }
        } catch (IOException e) {
            log("!!! Exception while running start process:");
            log(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log("====================");
    }


    @Override
    public final void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                tick();
            } catch(Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }

            try {
                //noinspection BusyWait
                Thread.sleep(NATURAL_TIMEOUT_MS);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}