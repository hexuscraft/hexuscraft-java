package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ServerManager {

    private final ServerMonitor _monitor;

    private final String _path;

    ServerManager(final ServerMonitor serverMonitor, final String path) {
        _monitor = serverMonitor;
        _path = path;
    }

    public Optional<Thread> startServer(final UnifiedJedis jedis, final ServerGroupData serverGroupData,
                                        final String reason) {
        final ServerData[] existingServers;
        try {
            existingServers = ServerQueries.getServers(jedis, serverGroupData);
        } catch (final JedisException ex) {
            _monitor.log(
                    "JedisException while getting servers for startServer(" + serverGroupData._name + ": " + reason +
                            "): " + ex.getMessage());
            return Optional.empty();
        }

        final Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
        for (ServerData existingServer : existingServers) {
            serverDataIdMap.put(existingServer._port - serverGroupData._minPort + 1, existingServer);
        }

        final AtomicInteger lowestId = new AtomicInteger(0);
        for (int i = 1; i <= (serverGroupData._maxPort - serverGroupData._minPort + 1); i++) {
            if (serverDataIdMap.containsKey(i)) continue;
            lowestId.set(i);
            break;
        }

        if (lowestId.get() == 0) {
            _monitor.log("Could not startServer(" + serverGroupData._name + ": " + reason + "): Max Port Reached");
            return Optional.empty();
        }

        int serverPort = serverGroupData._minPort + lowestId.get() - 1;

        final String serverName = serverGroupData._name + "-" + lowestId;
        _monitor.log(serverName + ": Starting: " + reason);

        try {
            new ServerData(serverName, "", serverGroupData._capacity, System.currentTimeMillis(), serverGroupData._name,
                    "", 0, serverPort, 20, System.currentTimeMillis(), true).update(jedis);
        } catch (final JedisException ex) {
            _monitor.log("JedisException while creating template server data for startServer(" + serverGroupData._name +
                    ": " + reason + "): " + ex.getMessage());
            return Optional.empty();
        }

        try {
            final Process process =
                    new ProcessBuilder(_path + "/Scripts/StartServer.cmd", serverGroupData._name + "-" + lowestId,
                            serverGroupData._name, Integer.toString(serverPort), Integer.toString(serverGroupData._ram),
                            Integer.toString(serverGroupData._capacity), serverGroupData._plugin,
                            serverGroupData._worldZip, Boolean.toString(serverGroupData._worldEdit)).start();

            final boolean finished = process.waitFor(serverGroupData._timeoutMillis, TimeUnit.MILLISECONDS);

            if (!finished) {
                process.destroy();
                throw new IOException(
                        "Aborting as start script did not finish within " + serverGroupData._timeoutMillis + "ms");
            }

            final int exitValue = process.exitValue();
            if (exitValue != 0) {
                final String errors;
                try (final BufferedReader reader = process.errorReader()) {
                    errors = reader.readAllAsString();
                }
                throw new IOException("Process exited with value '" + exitValue + "': " + errors);
            }
        } catch (final IOException ex) {
            _monitor.log(serverName + ": IOException while running start process: " + ex.getMessage());
            return Optional.empty();
        } catch (final InterruptedException ex) {
            _monitor.log(serverName + ": InterruptedException while running start process: " + ex.getMessage());
            return Optional.empty();
        }

        final Thread thread = new Thread(() -> {
            try {
                _monitor.log(serverName + ": Async-waiting for redis data...");

                final long startMs = System.currentTimeMillis();
                while (true) {
                    if ((System.currentTimeMillis() - startMs) > 30000L) {
                        killServer(jedis, serverName, serverGroupData._name, "Slow Start-up");
                        break;
                    }
                    final ServerData serverData = ServerQueries.getServer(jedis, serverName);
                    if (serverData != null && !serverData._updatedByMonitor) break;

                    //noinspection BusyWait
                    Thread.sleep(1L);
                }
                _monitor.log(serverName + ": Started");
            } catch (final InterruptedException ex) {
                _monitor.log(serverName + ": Exception while busy-waiting: " + ex.getMessage());
            }
        });
        thread.start();
        return Optional.of(thread);
    }

    public void killServer(final UnifiedJedis jedis, final String serverName, final String serverGroupName,
                           final String reason) {
        _monitor.log(serverName + ": Killing: " + reason);

        try {
            final Process process =
                    new ProcessBuilder(_path + "/scripts/killServer.cmd", serverName, serverGroupName).start();
            final boolean finished = process.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                process.destroy();
                throw new IOException("Aborting as process did not finish in 10 seconds");
            }

            final int exitValue = process.exitValue();
            if (exitValue != 0) {
                final String errors;
                try (final BufferedReader reader = process.errorReader()) {
                    errors = reader.readAllAsString();
                }
                throw new IOException("Process exited with value '" + exitValue + "': " + errors);
            }

            jedis.del(ServerQueries.SERVER(serverName));
            _monitor.log(serverName + ": Killed");
        } catch (final IOException ex) {
            _monitor.log(serverName + ": Exception while running kill process: " + ex.getMessage());
        } catch (final InterruptedException ex) {
            _monitor.log(serverName + ": Exception while waiting for kill process to finish: " + ex.getMessage());
        }
    }

}
