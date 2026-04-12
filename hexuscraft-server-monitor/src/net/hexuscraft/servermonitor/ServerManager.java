package net.hexuscraft.servermonitor;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManager
{

    final ServerMonitor _monitor;

    final String _path;

    ServerManager(ServerMonitor serverMonitor, String path)
    {
        _monitor = serverMonitor;
        _path = path;
    }

    public Optional<Thread> startServer(UnifiedJedis jedis, ServerGroupData serverGroupData, String reason)
    {
        ServerData[] existingServers;
        try
        {
            existingServers = ServerQueries.getServers(jedis, serverGroupData);
        }
        catch (JedisException ex)
        {
            _monitor.log("JedisException while getting servers for startServer(" +
                    serverGroupData._name +
                    ": " +
                    reason +
                    "): " +
                    ex.getMessage());
            return Optional.empty();
        }

        Map<Integer, ServerData> serverDataIdMap = new HashMap<>();
        for (ServerData existingServer : existingServers)
        {
            serverDataIdMap.put(existingServer._port - serverGroupData._minPort + 1, existingServer);
        }

        AtomicInteger lowestId = new AtomicInteger(0);
        for (int i = 1; i <= (serverGroupData._maxPort - serverGroupData._minPort + 1); i++)
        {
            if (serverDataIdMap.containsKey(i))
            {
                continue;
            }
            lowestId.set(i);
            break;
        }

        if (lowestId.get() == 0)
        {
            _monitor.log("Could not startServer(" + serverGroupData._name + ": " + reason + "): Max Port Reached");
            return Optional.empty();
        }

        String serverName = serverGroupData._name + "-" + lowestId;
        _monitor.log(serverName + ": Starting: " + reason);

        int serverPort = serverGroupData._minPort + lowestId.get() - 1;

        try
        {
            new ServerData(serverName,
                    "",
                    serverGroupData._capacity,
                    System.currentTimeMillis(),
                    serverGroupData._name,
                    "",
                    0,
                    serverPort,
                    20,
                    System.currentTimeMillis(),
                    true).update(jedis);
        }
        catch (JedisException ex)
        {
            _monitor.log("JedisException while creating template server data for startServer(" +
                    serverGroupData._name +
                    ": " +
                    reason +
                    "): " +
                    ex.getMessage());
            return Optional.empty();
        }

        try
        {
            Process process = new ProcessBuilder(_path + "/Scripts/StartServer.cmd",
                    serverGroupData._name + "-" + lowestId,
                    serverGroupData._name,
                    Integer.toString(serverPort),
                    Integer.toString(serverGroupData._ram),
                    Integer.toString(serverGroupData._capacity),
                    serverGroupData._plugin,
                    serverGroupData._worldZip,
                    Boolean.toString(serverGroupData._worldEdit)).start();

            boolean finished = process.waitFor(serverGroupData._timeoutMillis, TimeUnit.MILLISECONDS);

            if (!finished)
            {
                process.destroy();
                throw new IOException("Aborting as start script did not finish within " +
                        serverGroupData._timeoutMillis +
                        "ms");
            }

            int exitValue = process.exitValue();
            if (exitValue != 0)
            {
                String errors;
                try (BufferedReader reader = process.errorReader())
                {
                    errors = reader.readAllAsString();
                }
                throw new IOException("Process exited with value '" + exitValue + "': " + errors);
            }
        }
        catch (IOException ex)
        {
            _monitor.log(serverName + ": IOException while running start process: " + ex.getMessage());
            return Optional.empty();
        }
        catch (InterruptedException ex)
        {
            _monitor.log(serverName + ": InterruptedException while running start process: " + ex.getMessage());
            return Optional.empty();
        }

        Thread thread = new Thread(() ->
        {
            try
            {
                _monitor.log(serverName + ": Async-waiting for redis data...");

                long startMs = System.currentTimeMillis();
                while (true)
                {
                    if ((System.currentTimeMillis() - startMs) > 30000L)
                    {
                        killServer(jedis, serverName, serverGroupData._name, "Slow Start-up");
                        break;
                    }
                    ServerData serverData = ServerQueries.getServer(jedis, serverName);
                    if (serverData != null && !serverData._updatedByMonitor)
                    {
                        break;
                    }

                    //noinspection BusyWait
                    Thread.sleep(1L);
                }
                _monitor.log(serverName + ": Started");
            }
            catch (InterruptedException ex)
            {
                _monitor.log(serverName + ": Exception while busy-waiting: " + ex.getMessage());
            }
        });
        thread.start();
        return Optional.of(thread);
    }

    public void killServer(UnifiedJedis jedis, String serverName, String serverGroupName, String reason)
    {
        _monitor.log(serverName + ": Killing: " + reason);

        try
        {
            Process process =
                    new ProcessBuilder(_path + "/scripts/killServer.cmd", serverName, serverGroupName).start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);

            if (!finished)
            {
                process.destroy();
                throw new IOException("Aborting as process did not finish in 10 seconds");
            }

            int exitValue = process.exitValue();
            if (exitValue != 0)
            {
                String errors;
                try (BufferedReader reader = process.errorReader())
                {
                    errors = reader.readAllAsString();
                }
                throw new IOException("Process exited with value '" + exitValue + "': " + errors);
            }

            jedis.del(ServerQueries.SERVER(serverName));
            _monitor.log(serverName + ": Killed");
        }
        catch (IOException ex)
        {
            _monitor.log(serverName + ": Exception while running kill process: " + ex.getMessage());
        }
        catch (InterruptedException ex)
        {
            _monitor.log(serverName + ": Exception while waiting for kill process to finish: " + ex.getMessage());
        }
    }

}
