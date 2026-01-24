package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public final class ServerMonitor implements Runnable {

    private final String NETWORK_SPY_CHANNEL = "NetworkSpy";

    private final Console _console;
    private final Database _database;
    private final ServerManager _manager;
    @SuppressWarnings("FieldCanBeLocal")
    private final InetAddress _inetAddress;
    private final Map<String, ServerData> _serverDataMap;
    private final Map<String, ServerGroupData> _serverGroupDataMap;

    private ServerMonitor(final String[] args) throws UnknownHostException, FileNotFoundException {
        _console = System.console();
        _database = new Database();
        _manager = new ServerManager(this, new Scanner(new File("_path.dat")).nextLine());
        _inetAddress = InetAddress.getByName(args.length > 0 ? args[0] : "127.0.0.1");
        _serverDataMap = new HashMap<>();
        _serverGroupDataMap = new HashMap<>();

        new Thread(this).start();
    }

    static void main(final String[] args) {
        try {
            new ServerMonitor(args);
        } catch (UnknownHostException | FileNotFoundException ex) {
            System.out.println("Exception while instantiating: " + String.join("\n", Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new)));
        }
    }

    public void log(final String message) {
        _console.printf("\n[" + System.currentTimeMillis() + "] " + message);
        new Thread(() -> _database._jedis.publish(NETWORK_SPY_CHANNEL, message)).start();
    }

    private void tick() {
        _serverDataMap.clear();
        _serverDataMap.putAll(ServerQueries.getServersAsMap(_database._jedis));

        _serverGroupDataMap.clear();
        _serverGroupDataMap.putAll(ServerQueries.getServerGroupsAsMap(_database._jedis));

        final Map<ServerGroupData, Set<ServerData>> totalServersMap = new HashMap<>();
        final Map<ServerGroupData, Set<ServerData>> joinableServersMap = new HashMap<>();

        _serverGroupDataMap.values().forEach(serverGroupData -> {
            totalServersMap.put(serverGroupData, new HashSet<>());
            joinableServersMap.put(serverGroupData, new HashSet<>());
        });

        for (final ServerData serverData : _serverDataMap.values()) {
            final ServerGroupData serverGroupData = _serverGroupDataMap.get(serverData._group);
            if (serverGroupData == null) {
                _manager.killServer(_database._jedis, serverData._name, serverData._group, "Invalid Server Group");
                return;
            }

            if (serverData._port < serverGroupData._minPort || serverData._port > serverGroupData._maxPort) {
                _manager.killServer(_database._jedis, serverData._name, serverData._group, "Port Outside Range");
                return;
            }

            final List<String> motdStrings = Arrays.stream(serverData._motd.split(",")).toList();
            if (motdStrings.contains("DEAD")) {
                _manager.killServer(_database._jedis, serverData._name, serverData._group, "Dead");
                return;
            }

            if ((System.currentTimeMillis() - serverData._updated) > (serverData._updatedByMonitor ? Math.max(30000L, serverGroupData._timeoutMillis) : serverGroupData._timeoutMillis)) {
                _manager.killServer(_database._jedis, serverData._name, serverData._group, "Unresponsive");
                return;
            }

            totalServersMap.get(serverGroupData).add(serverData);
            if (motdStrings.contains("LIVE_GAME")) continue;
            if (serverData._players >= serverData._capacity) continue;
            joinableServersMap.get(serverGroupData).add(serverData);
        }

        for (final ServerGroupData serverGroupData : _serverGroupDataMap.values().stream().sorted(Comparator.comparingInt(value -> value._minPort)).toArray(ServerGroupData[]::new)) {
            final Set<ServerData> totalServers = totalServersMap.get(serverGroupData);
            final int totalServersAmount = totalServers.size();

            final Set<ServerData> joinableServers = joinableServersMap.get(serverGroupData);
            final int joinableServersAmount = joinableServers.size();

            final boolean isEnoughTotalServers = totalServersAmount >= serverGroupData._totalServers;
            final boolean isOverflowTotalServers = totalServersAmount > serverGroupData._totalServers;

            final boolean isEnoughJoinableServers = joinableServersAmount >= serverGroupData._joinableServers;
            final boolean isOverflowJoinableServers = joinableServersAmount > serverGroupData._joinableServers;

            // Kill excess servers
            if (isOverflowTotalServers && isOverflowJoinableServers) {
                final ServerData bestServerToKill = getBestServerToKill(_database._jedis, serverGroupData);
                if (bestServerToKill != null) {
                    _manager.killServer(_database._jedis, bestServerToKill._name, bestServerToKill._group, "Excess Servers");
                    return;
                }
            }

            // Start minimum servers
            if (!isEnoughTotalServers || !isEnoughJoinableServers) {
                _manager.startServer(_database._jedis, serverGroupData, "Insufficient Servers");
            }
        }

    }

    private ServerData getBestServerToKill(final UnifiedJedis jedis, final ServerGroupData serverGroupData) {
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
            } catch (final Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }

            try {
                //noinspection BusyWait
                Thread.sleep(500L);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}