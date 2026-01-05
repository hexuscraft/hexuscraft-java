package net.hexuscraft.core.database;

import net.hexuscraft.common.database.Database;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.UnifiedJedis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class MiniPluginDatabase extends MiniPlugin<HexusPlugin> {

    private final Database _database;
    private final Map<String, Map<PubSubConsumer, JedisPubSub>> _consumers;

    public MiniPluginDatabase(final HexusPlugin plugin) {
        super(plugin, "Database");

        _consumers = new HashMap<>();

        final AtomicReference<String> atomicHost = new AtomicReference<>("127.0.0.1");
        final AtomicInteger atomicPort = new AtomicInteger(6379);
        final AtomicReference<String> atomicUsername = new AtomicReference<>("default");
        final AtomicReference<String> atomicPassword = new AtomicReference<>("");

        try {
            final File redisFile = new File("_redis.dat");
            final Scanner redisScanner = new Scanner(redisFile);
            atomicHost.set(redisScanner.nextLine());
            if (redisScanner.hasNextLine()) {
                atomicPort.set(Integer.parseInt(redisScanner.nextLine()));
                if (redisScanner.hasNextLine()) {
                    atomicUsername.set(redisScanner.nextLine());
                    if (redisScanner.hasNextLine()) atomicPassword.set(redisScanner.nextLine());
                }
            }
            redisScanner.close();
        } catch (final NullPointerException | NoSuchElementException _) {
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        try {
            _database = new Database(atomicHost.get(), atomicPort.get(), atomicUsername.get(), atomicPassword.get());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onDisable() {
        _consumers.clear();
    }

    public UnifiedJedis getUnifiedJedis() {
        return _database._unifiedJedis;
    }

    public void registerConsumer(final String pattern, final PubSubConsumer consumer) {
        final Map<PubSubConsumer, JedisPubSub> consumerMap;
        if (_consumers.containsKey(pattern)) consumerMap = _consumers.get(pattern);
        else {
            consumerMap = new HashMap<>();
            _consumers.put(pattern, consumerMap);
        }

        final JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onPMessage(final String pattern, final String channelName, final String message) {
                consumer.accept(pattern, channelName, message);
            }

            @Override
            public void onPSubscribe(final String pattern, final int subscribedChannels) {
                logInfo("Subscribed to '" + pattern + "'. Subscribed channels: " + subscribedChannels);
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
                logInfo("Unsubscribed from '" + pattern + "'. Subscribed channels: " + subscribedChannels);
            }
        };

        consumerMap.put(consumer, jedisPubSub);
        _hexusPlugin.runAsync(() -> getUnifiedJedis().psubscribe(jedisPubSub, pattern));
    }

    public void unregisterConsumer(final PubSubConsumer consumer) {
        _consumers.forEach((pattern, consumerMap) -> {
            consumerMap.remove(consumer);
            if (!consumerMap.isEmpty()) return;
            _consumers.remove(pattern);
        });
    }

    public void unregisterConsumers() {
        _consumers.clear();
    }

}