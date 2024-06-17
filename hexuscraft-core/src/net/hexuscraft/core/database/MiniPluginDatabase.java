package net.hexuscraft.core.database;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.database.Database;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public final class MiniPluginDatabase extends MiniPlugin<HexusPlugin> {

    private final Database _database;

    private final Map<String, Map<UUID, MessagedRunnable>> _callbacks;

    private BukkitTask _asyncMessageTask;

    public MiniPluginDatabase(final HexusPlugin plugin) {
        super(plugin, "Database");

        _callbacks = new HashMap<>();

        final String host;
        final int port;

        try {
            final Scanner redisScanner = new Scanner(new File("_redis.dat"));
            host = redisScanner.nextLine();
            port = redisScanner.nextInt();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        try {
            _database = new Database(host, port);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onEnable() {
        _asyncMessageTask = _hexusPlugin.getServer().getScheduler().runTaskAsynchronously(_hexusPlugin, () -> {
            while (true) {
                try {
                    getJedisPooled().psubscribe(new JedisPubSub() {

                        @Override
                        public void onPMessage(String pattern, String channel, String message) {
                            if (!_callbacks.containsKey(channel)) return;
                            _callbacks.get(channel).forEach((uuid, callback) -> {
                                callback.setMessage(message);
                                callback.run();
                            });
                        }

                        @Override
                        public void onPSubscribe(String pattern, int subscribedChannels) {
//                            log("[JEDIS] Subscribed to '" + pattern + "' (" + subscribedChannels + ")");
                        }

                    }, "*");
                } catch (JedisConnectionException ex) {
                    log("[JEDIS] Exception while connecting to database: " + ex.getMessage());
                } catch (Exception ex) {
                    break;
                }
            }
        });
    }

    @Override
    public void onDisable() {
        _callbacks.clear();
        if (_asyncMessageTask != null) _asyncMessageTask.cancel();
    }

    public JedisPooled getJedisPooled() {
        return _database._jedisPooled;
    }

    @SuppressWarnings("UnusedReturnValue")
    public UUID registerCallback(final String channelName, final MessagedRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public void unregisterCallback(final UUID id) {
        _callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) {
                return;
            }
            uuidRunnableMap.remove(id);

            // remove the map if there are no more callbacks
            if (!uuidRunnableMap.values().isEmpty()) {
                return;
            }
            _callbacks.remove(s);
        });
    }

}