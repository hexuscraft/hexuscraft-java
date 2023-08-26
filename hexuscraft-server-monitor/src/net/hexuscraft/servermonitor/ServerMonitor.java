package net.hexuscraft.servermonitor;

import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.servermonitor.database.PluginDatabase;
import redis.clients.jedis.JedisPubSub;

import java.io.Console;
import java.util.Map;
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
        _database.getJedisPooled().smembers(ServerQueries.SERVERS_QUEUE()).stream().map(UUID::fromString).forEach(uuid -> {
            _database.getJedisPooled().srem(ServerQueries.SERVERS_QUEUE(), uuid.toString());
            log("Starting server with UUID '" + uuid + "'...");
        });

        _database.getJedisPooled().smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).forEach(uuid -> {
            final ServerData serverData = new ServerData(_database.getJedisPooled().hgetAll(ServerQueries.SERVER(uuid)));
            if ((System.currentTimeMillis() - serverData._lastUpdate) > 5000L) {
                _database.getJedisPooled().srem(ServerQueries.SERVERS_ACTIVE(), uuid.toString());
                log("Server with uuid '" + uuid + "' has not updated within 5 seconds. Removed it from the 'servers.active' set.");
            }
        });
    }

    @Override
    public final void run() {
        new Thread(() -> {
            while (true) {
                try {
                    _database.getJedisPooled().psubscribe(new JedisPubSub() {
                        @Override
                        public void onPMessage(final String pattern, final String channel, final String message) {
                            // TODO: Messaging logic
                        }
                    }, "*");
                } catch(final Exception ex) {
                    log("Disconnected from redis subscription: " + ex.getMessage());
                    break;
                }
            }
        }).start();

        while (true) {
            try {
                tick();
                //noinspection BusyWait
                Thread.sleep(100);
            } catch(final InterruptedException ex) {
                log("Interrupted, stopping monitor loop.");
                break;
            } catch (final Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }
        }
    }

}