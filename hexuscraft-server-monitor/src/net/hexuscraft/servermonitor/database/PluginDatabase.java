package net.hexuscraft.servermonitor.database;

import net.hexuscraft.common.database.Database;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public final class PluginDatabase {

    private final Database _database;

    private final Map<String, Map<UUID, MessagedRunnable>> _callbacks;

    public PluginDatabase() {
        _callbacks = new HashMap<>();

        // Default redis config
        String host = "127.0.0.1";
        int port = 6379;
        String username = "default";
        String password = "";

        try {
            final File redisFile = new File("_redis.dat");
            final Scanner redisScanner = new Scanner(redisFile);
            host = redisScanner.nextLine();
            if (redisScanner.hasNextLine()) {
                port = Integer.parseInt(redisScanner.nextLine());
                if (redisScanner.hasNextLine()) {
                    username = redisScanner.nextLine();
                    if (redisScanner.hasNextLine()) password = redisScanner.nextLine();
                }
            }
            redisScanner.close();
        } catch (final NullPointerException | NoSuchElementException _) {
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        try {
            _database = new Database(host, port, username, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            while (true) {
                try {
                    getUnifiedJedis().psubscribe(new JedisPubSub() {

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

    public UnifiedJedis getUnifiedJedis() {
        return _database._unifiedJedis;
    }

    public UUID registerCallback(String channelName, MessagedRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    public void unregisterCallback(UUID id) {
        _callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) return;
            uuidRunnableMap.remove(id);

            // remove the map if there are no more callbacks
            if (!uuidRunnableMap.isEmpty()) return;
            _callbacks.remove(s);
        });
    }

}