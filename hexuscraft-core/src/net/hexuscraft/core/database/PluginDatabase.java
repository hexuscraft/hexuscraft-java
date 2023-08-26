package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.database.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class PluginDatabase extends MiniPlugin {

    private final Database _database;

    private final Map<String, Map<UUID, MessagedRunnable>> _callbacks;

    private BukkitRunnable _asyncMessageRunnable;

    public PluginDatabase(JavaPlugin javaPlugin) {
        super(javaPlugin, "Database");

        _callbacks = new HashMap<>();

        final String host;
        final int port;

        try {
            File redisFile = new File("_redis.dat");
            Scanner redisScanner = new Scanner(redisFile);
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
        _asyncMessageRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        getJedisPooled().psubscribe(new JedisPubSub() {

                            @Override
                            public void onPMessage(String pattern, String channel, String message) {
                                if (!_callbacks.containsKey(channel)) {
                                    return;
                                }
                                _callbacks.get(channel).forEach((uuid, callback) -> {
                                    callback.setMessage(message);
                                    callback.run();
                                });
                            }

                        }, "*");
                    } catch(JedisConnectionException ex) {
                        log("[JEDIS] Exception while connecting to database: " + ex.getMessage());
                    } catch(Exception ex) {
                        break;
                    }
                }
            }
        };
        _asyncMessageRunnable.runTaskAsynchronously(_javaPlugin);
    }

    @Override
    public void onDisable() {
        _callbacks.clear();
        _asyncMessageRunnable.cancel();
    }

    public JedisPooled getJedisPooled() {
        return _database._jedisPooled;
    }

    @SuppressWarnings("UnusedReturnValue")
    public UUID registerCallback(String channelName, MessagedRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public void unregisterCallback(UUID id) {
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