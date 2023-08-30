package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.servermonitor.database.PluginDatabase;
import redis.clients.jedis.JedisPooled;

import java.io.Console;
import java.util.UUID;

public class ServerMonitor implements Runnable {

    public static void main(String[] args) {
        new ServerMonitor();
    }

    private final Console _console;
    private final PluginDatabase _database;

    private ServerMonitor() {
        _console = System.console();
        _database = new PluginDatabase();
        new Thread(this).start();
    }

    private void log(final String message, final Object... args) {
        _console.printf(message + "\n", args);
    }

    private void tick() {
        JedisPooled jedis = _database.getJedisPooled();

        jedis.smembers(ServerQueries.SERVERS_QUEUE()).stream().map(UUID::fromString).forEach(uuid -> {
            jedis.srem(ServerQueries.SERVERS_QUEUE(), uuid.toString());
            log("Starting server with UUID '" + uuid + "'...");
        });

        jedis.smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).forEach(uuid -> {
            final ServerData serverData = new ServerData(uuid, jedis.hgetAll(ServerQueries.SERVER(uuid)));
            if ((System.currentTimeMillis() - serverData._lastUpdate) > 5000L) {
                jedis.srem(ServerQueries.SERVERS_ACTIVE(), uuid.toString());
                log("Server with uuid '" + uuid + "' has not updated within 5 seconds. Removed it from the 'servers.active' set.");
            }
        });
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
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}