package net.hexuscraft.core.database;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.Database;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class PluginDatabase extends MiniPlugin {

    PluginPortal pluginPortal;

    Database database;

    Map<String, Map<UUID, MessagedRunnable>> callbacks;

    public PluginDatabase(JavaPlugin javaPlugin) {
        super(javaPlugin, "Database");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        pluginPortal = (PluginPortal) dependencies.get(PluginPortal.class);

        callbacks = new HashMap<>();

        //noinspection ReassignedVariable
        String host = "127.0.0.1";
        //noinspection ReassignedVariable
        int port = 6379;

        File redisFile = new File("_redis.dat");
        try {
            Scanner redisScanner = new Scanner(redisFile);
            host = redisScanner.nextLine();
            port = redisScanner.nextInt();
        } catch (FileNotFoundException ex) {
            // Just realised the host and port fallback to defaults specified above. Stack trace here is A-OK!
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }

        try {
            database = new Database(host, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        _javaPlugin.getServer().getScheduler().runTaskAsynchronously(_javaPlugin, () -> getJedisPooled().psubscribe(new JedisPubSub() {

            @Override
            public void onPMessage(String pattern, String channel, String message) {
                if (!callbacks.containsKey(channel)) {
                    return;
                }
                callbacks.get(channel).forEach((uuid, callback) -> {
                    callback.setMessage(message);
                    callback.run();
                });
            }

        }, "*"));
    }

    @Override
    public void onDisable() {
        callbacks.clear();
    }

    public JedisPooled getJedisPooled() {
        return database.getJedisPooled();
    }

    @SuppressWarnings("UnusedReturnValue")
    public UUID registerCallback(String channelName, MessagedRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!callbacks.containsKey(channelName)) {
            callbacks.put(channelName, new HashMap<>());
        }
        callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public void unregisterCallback(UUID id) {
        callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) {
                return;
            }
            uuidRunnableMap.remove(id);

            // remove the map if there are no more callbacks
            if (!uuidRunnableMap.values().isEmpty()) {
                return;
            }
            callbacks.remove(s);
        });
    }

}