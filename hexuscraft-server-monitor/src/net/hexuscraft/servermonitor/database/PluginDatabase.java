package net.hexuscraft.servermonitor.database;

import net.hexuscraft.database.Database;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class PluginDatabase {

    private final Database _database;

    private final Map<String, Map<UUID, MessagedRunnable>> _callbacks;

    public PluginDatabase() {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
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
                            System.out.println("[JEDIS] Subscribed to '" + pattern + "' (" + subscribedChannels + ")");
                        }
                    }, "*");
                } catch (JedisConnectionException ex) {
                    System.out.println("[JEDIS] Exception while connecting to database: " + ex.getMessage());
                } catch (Exception ex) {
                    break;
                }
            }
        });
    }

    public JedisPooled getJedisPooled() {
        return _database._jedisPooled;
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
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
            if (!uuidRunnableMap.containsKey(id)) return;
            uuidRunnableMap.remove(id);

            // remove the map if there are no more callbacks
            if (!uuidRunnableMap.values().isEmpty()) return;
            _callbacks.remove(s);
        });
    }

}