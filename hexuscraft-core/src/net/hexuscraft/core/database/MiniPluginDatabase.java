package net.hexuscraft.core.database;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.database.Database;
import org.bukkit.Warning;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public final class MiniPluginDatabase extends MiniPlugin<HexusPlugin> {

    private final Database _database;

    private final Map<String, Map<UUID, MessagedRunnable>> _callbacks;

    private BukkitTask _asyncMessageTask;

    public MiniPluginDatabase(final HexusPlugin plugin) {
        super(plugin, "Database");

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
                        public void onPMessage(final String pattern, final String channelName, final String message) {
                            if (!_callbacks.containsKey(channelName)) return;
                            if (_callbacks.containsKey("*")) _callbacks.get("*").forEach((_, callback) -> {
                                callback.setPattern(pattern);
                                callback.setChannelName(channelName);
                                callback.setMessage(message);
                                callback.run();
                            });
                            _callbacks.get(channelName).forEach((_, callback) -> {
                                callback.setMessage(message);
                                callback.run();
                            });
                        }

                        @Override
                        public void onPSubscribe(String pattern, int subscribedChannels) {
                            _hexusPlugin.logInfo("[JEDIS] Subscribed to '" + pattern + "' (" + subscribedChannels + ")");
                        }

                    }, "*");
                } catch (JedisConnectionException ex) {
                    logInfo("[JEDIS] Exception while connecting to database: " + ex.getMessage());
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

    @Warning(reason = "Ensure async is used before ignoring")
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
            if (!uuidRunnableMap.isEmpty()) {
                return;
            }
            _callbacks.remove(s);
        });
    }

}