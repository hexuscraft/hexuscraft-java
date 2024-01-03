package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerManager {

    private final ServerMonitor _monitor;

    private final String _path;

    protected ServerManager(final ServerMonitor serverMonitor, final String path) {
        _monitor = serverMonitor;
        _path = path;
    }

    public final void startServer(final JedisPooled jedis, final ServerGroupData serverGroupData, final String reason) {
        _monitor.log("=== START SERVER ===");
        _monitor.log("Reason: " + reason);
        _monitor.log("Group: " + serverGroupData._name);
        _monitor.log("Ram: " + serverGroupData._ram);

        final ServerData[] existingServers = ServerQueries.getServers(jedis, serverGroupData);

        final Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
        for (ServerData existingServer : existingServers) {
            serverDataIdMap.put(Integer.parseInt(existingServer._name.split("-")[1]), existingServer);
        }

        //noinspection ReassignedVariable
        int lowestId = 0;
        for (int i = 1; i <= (serverGroupData._maxPort - serverGroupData._minPort); i++) {
            if (serverDataIdMap.containsKey(i)) continue;
            lowestId = i;
            break;
        }

        if (lowestId == 0) {
            _monitor.log("FATAL: Maximum port reached. Cancelling.");
            _monitor.log("====================");
            return;
        }

        final String serverName = serverGroupData._name + "-" + lowestId;
        _monitor.log("Name: " + serverName);

        try {
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "start",
                    _path + "/scripts/startServer.cmd",
                    "127.0.0.1",
                    "193.110.160.24",
                    Integer.toString(serverGroupData._minPort + lowestId),
                    Integer.toString(serverGroupData._ram),
                    serverGroupData._worldZip,
                    serverGroupData._plugin,
                    serverGroupData._name,
                    serverGroupData._name + "-" + lowestId,
                    "false",
                    Integer.toString(serverGroupData._capacity)
            ).start();
            _monitor.log("Waiting for server to startup...");

            final long startMs = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - startMs) > 30000L) {
                    killServer(jedis, serverName, "Slow Start-up");
                    break;
                }
                if (ServerQueries.getServer(jedis, serverName) != null) break;

                //noinspection BusyWait
                Thread.sleep(1000L);
            }
        } catch (final IOException e) {
            _monitor.log("!!! Exception while running start process:");
            _monitor.log(e.getMessage(), e);
        } catch (final InterruptedException e) {
            _monitor.log("!!! Exception while busy-waiting:");
            _monitor.log(e.getMessage(), e);
        } finally {
            _monitor.log("====================");
        }
    }

    public final void killServer(final JedisPooled jedis, final String serverName, final String reason) {
        _monitor.log("=== KILL SERVER ===");
        _monitor.log("Reason: " + reason);
        _monitor.log("Name: " + serverName);

        try {
            new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "start",
                    _path + "/scripts/killServer.cmd",
                    "127.0.0.1",
                    "193.110.160.24",
                    serverName
            ).start();
            jedis.del(ServerQueries.SERVER(serverName));
            _monitor.log("Waiting for server to die...");

            Thread.sleep(3000L);
        } catch (final IOException ex) {
            _monitor.log("!!! Exception while running kill process:");
            _monitor.log(ex.getMessage(), ex);
        } catch (final InterruptedException ex) {
            _monitor.log("!!! Exception while busy-waiting:");
            _monitor.log(ex.getMessage(), ex);
        } finally {
            _monitor.log("====================");
        }
    }

}
