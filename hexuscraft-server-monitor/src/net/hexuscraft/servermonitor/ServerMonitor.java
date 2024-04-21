package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.hexuscraft.servermonitor.database.PluginDatabase;
import redis.clients.jedis.JedisPooled;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ServerMonitor implements Runnable {

    public static void main(final String[] args) {
        new ServerMonitor();
    }

    private final Console _console;
    private final PluginDatabase _database;
    private final ServerManager _manager;

    private final Map<String, ServerData> _serverDataMap;
    private final Map<String, ServerGroupData> _serverGroupDataMap;
    private final Set<String> _deadServersSet;

    private ServerMonitor() {
        _console = System.console();
        _database = new PluginDatabase();

        final String path;
        try {
            File pathFile = new File("_path.dat");
            Scanner pathScanner = new Scanner(pathFile);
            path = pathScanner.nextLine();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        _manager = new ServerManager(this, path);

        _serverDataMap = new HashMap<>();
        _serverGroupDataMap = new HashMap<>();
        _deadServersSet = new HashSet<>();

        new Thread(this).start();
    }

    public final void log(final String message, final Object... args) {
        _console.printf("[" + System.currentTimeMillis() + "] " + message + "\n", args);
    }

    private void tick() {
        final JedisPooled jedis = _database.getJedisPooled();

        _deadServersSet.clear();
        _deadServersSet.addAll(ServerQueries.getDeadServers(jedis));

        for (String name : _deadServersSet) {
            _manager.killServer(jedis, name, "Dead");
        }

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

            // Kill unresponsive servers
            if ((System.currentTimeMillis() - serverData._updated) > 10000L) {
                _manager.killServer(jedis, serverData._name, "Unresponsive");
                return;
            }

            final ServerGroupData serverGroupData = _serverGroupDataMap.get(serverData._group);

            // Kill servers without a valid server group
            if (serverGroupData == null) {
                _manager.killServer(jedis, serverData._name, "Invalid Server Group");
                return;
            }

            // Count total and joinable servers
            totalServersMap.get(serverGroupData).add(serverData);

            if (serverData._motd.startsWith("LIVE")) return;
            if (serverData._players >= serverData._capacity) return;

            joinableServersMap.get(serverGroupData).add(serverData);
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
            if (!isEnoughTotalServers || !isEnoughJoinableServers) {
                _manager.startServer(jedis, serverGroupData, "Insufficient Servers");
            }

            // Kill excess servers
            if (isOverflowTotalServers && isOverflowJoinableServers) {
                final ServerData bestServerToKill = getBestServerToKill(jedis, serverGroupData);
                if (bestServerToKill != null)
                    _manager.killServer(jedis, bestServerToKill._name, "Excess Servers");
            }

        });
    }

    private ServerData getBestServerToKill(final JedisPooled jedis, final ServerGroupData serverGroupData) {
        for (ServerData serverData : ServerQueries.getServers(jedis, serverGroupData)) {
            if (serverData._motd.startsWith("LIVE")) continue;
            if (serverData._players > (serverData._capacity / 3)) continue;
            return serverData;
        }
        return null;
    }

    @Override
    public final void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                tick();
            } catch (final Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }

            try {
                //noinspection BusyWait
                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}