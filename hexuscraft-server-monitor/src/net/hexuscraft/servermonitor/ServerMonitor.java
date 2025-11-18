package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.hexuscraft.servermonitor.database.PluginDatabase;
import redis.clients.jedis.JedisPooled;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.*;

public final class ServerMonitor implements Runnable {

    static void main(final String[] args) {
        new ServerMonitor(args);
    }

    private final Console _console;
    private final PluginDatabase _database;
    private final ServerManager _manager;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final InetAddress _inetAddress;

    private final Map<String, ServerData> _serverDataMap;
    private final Map<String, ServerGroupData> _serverGroupDataMap;

    public int _actionDepth;

    private ServerMonitor(final String[] args) {
        _console = System.console();
        _database = new PluginDatabase();
        _actionDepth = -1;

        //noinspection ReassignedVariable
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(args[0]);
        } catch (final Exception ex) {
            log("Error getting InetAddress: " + ex.getMessage());
        } finally {
            _inetAddress = inetAddress;
        }

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

        new Thread(this).start();
    }

    public void log(final String message, final Object... args) {
        final StringBuilder builder = new StringBuilder();
        if (_actionDepth > 0) builder.append(">".repeat(_actionDepth)).append(" ");
        builder.append(message).append("\n");

        _console.printf("[" + System.currentTimeMillis() + "] " + builder, args);
        new Thread(() -> _database.getJedisPooled().publish("NetworkSpy", builder.toString())).start();
    }

    private void tick() {
        final JedisPooled jedis = _database.getJedisPooled();

        _serverDataMap.clear();
        _serverDataMap.putAll(ServerQueries.getServersAsMap(jedis));

        _serverGroupDataMap.clear();
        _serverGroupDataMap.putAll(ServerQueries.getServerGroupsAsMap(jedis));

        final Map<ServerGroupData, Set<ServerData>> totalServersMap = new HashMap<>();
        final Map<ServerGroupData, Set<ServerData>> joinableServersMap = new HashMap<>();

        _serverGroupDataMap.values().forEach(serverGroupData -> {
            totalServersMap.put(serverGroupData, new HashSet<>());
            joinableServersMap.put(serverGroupData, new HashSet<>());
        });

        for (final ServerData serverData : _serverDataMap.values()) {
            // Kill servers without a valid server group
            final ServerGroupData serverGroupData = _serverGroupDataMap.get(serverData._group);
            if (serverGroupData == null) {
                _manager.killServer(jedis, serverData._name, "Invalid Server Group");
                return;
            }

            // Kill servers marked as dead
            final List<String> motdStrings = Arrays.stream(serverData._motd.split(",")).toList();
            if (motdStrings.contains("DEAD")) {
                _manager.killServer(jedis, serverData._name, "Dead");
                return;
            }

            // Kill unresponsive servers
            if ((System.currentTimeMillis() - serverData._updated) > serverGroupData._timeoutMillis) {
                _manager.killServer(jedis, serverData._name, "Unresponsive");
                return;
            }

            // Count total and joinable servers
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
                final ServerData bestServerToKill = getBestServerToKill(jedis, serverGroupData);
                if (bestServerToKill != null) {
                    _manager.killServer(jedis, bestServerToKill._name, "Excess Servers");
                    return;
                }
            }

            // Start minimum servers
            if (!isEnoughTotalServers || !isEnoughJoinableServers) {
                _manager.startServer(jedis, serverGroupData, "Insufficient Servers");
                return;
            }
        }

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
                Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}