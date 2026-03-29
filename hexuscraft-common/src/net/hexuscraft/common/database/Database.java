package net.hexuscraft.common.database;

import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Database
{

    public static String KEY_DELIMITER = ":";
    private final Map<String, Map<PubSubConsumer, JedisPubSub>> _consumers;
    public UnifiedJedis _jedis;

    // TODO::::::::: https://redis.io/docs/latest/develop/clients/jedis/produsage/

    public Database(String host, int port, String username, String password, String clientName) throws JedisException
    {
        _consumers = new HashMap<>();
        _jedis = buildUnifiedJedis(host, port, username, password, clientName);
    }

    public Database()
    {
        AtomicReference<String> atomicHost = new AtomicReference<>("127.0.0.1");
        AtomicInteger atomicPort = new AtomicInteger(6379);
        AtomicReference<String> atomicUsername = new AtomicReference<>("default");
        AtomicReference<String> atomicPassword = new AtomicReference<>("");

        File redisFile = new File("_redis.dat");
        try (Scanner scanner = new Scanner(redisFile))
        {
            ((Runnable) () ->
            {
                if (!scanner.hasNextLine())
                {
                    return;
                }
                atomicHost.set(scanner.nextLine());

                if (!scanner.hasNextLine())
                {
                    return;
                }
                atomicPort.set(Integer.parseInt(scanner.nextLine()));

                if (!scanner.hasNextLine())
                {
                    return;
                }
                atomicUsername.set(scanner.nextLine());

                if (!scanner.hasNextLine())
                {
                    return;
                }
                atomicPassword.set(scanner.nextLine());
            }).run();
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("WARNING: Could not locate '" +
                               redisFile.getName() +
                               "'. Using default credentials '" +
                               atomicUsername.get() +
                               ":" +
                               atomicPassword.get() +
                               "@" +
                               atomicHost.get() +
                               ":" +
                               atomicPort.get() +
                               "'.");
        }

        AtomicReference<String> clientName = new AtomicReference<>();
        File nameFile = new File("_name.dat");
        try (Scanner scanner = new Scanner(nameFile))
        {
            clientName.set(scanner.nextLine());
        }
        catch (FileNotFoundException ex)
        {
            clientName.set(new Random().ints(16, 0, 36)
                                       .mapToObj("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"::charAt)
                                       .map(String::valueOf)
                                       .collect(Collectors.joining()));
            System.out.println("WARNING: Could not locate '" +
                               nameFile.getName() +
                               "'. Using random client name '" +
                               clientName.get() +
                               "'.");
        }

        _consumers = new HashMap<>();
        _jedis = buildUnifiedJedis(atomicHost.get(),
                                   atomicPort.get(),
                                   atomicUsername.get(),
                                   atomicPassword.get(),
                                   clientName.get());
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
    }

    public static String buildQuery(String... args)
    {
        return String.join(KEY_DELIMITER, args);
    }

    private UnifiedJedis buildUnifiedJedis(String host, int port, String username, String password, String clientName)
    {
        HostAndPort hostAndPort = new HostAndPort(host, port);

        ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();
        connectionPoolConfig.setMaxTotal(32);
        connectionPoolConfig.setMaxIdle(connectionPoolConfig.getMaxTotal());
        connectionPoolConfig.setMinIdle(0);
        connectionPoolConfig.setBlockWhenExhausted(true);
        connectionPoolConfig.setMaxWait(Duration.ofSeconds(1));
        connectionPoolConfig.setTestWhileIdle(true);
        connectionPoolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                                                                      .clientName(clientName)
                                                                      .database(0)
                                                                      .user(username)
                                                                      .password(password)
                                                                      .build();

        return RedisClient.builder()
                          .clientConfig(jedisClientConfig)
                          .hostAndPort(hostAndPort)
                          .poolConfig(connectionPoolConfig)
                          .build();
    }

    public void registerConsumer(String pattern, PubSubConsumer consumer)
    {
        Map<PubSubConsumer, JedisPubSub> consumerMap;
        if (_consumers.containsKey(pattern))
        {
            consumerMap = _consumers.get(pattern);
        }
        else
        {
            consumerMap = new HashMap<>();
            _consumers.put(pattern, consumerMap);
        }

        JedisPubSub jedisPubSub = new JedisPubSub()
        {
            @Override
            public void onPMessage(String pattern, String channelName, String rawMessage)
            {
                consumer.accept(pattern, channelName, rawMessage);
            }
        };

        consumerMap.put(consumer, jedisPubSub);
        new Thread(() -> _jedis.psubscribe(jedisPubSub, pattern)).start();
    }

    public void unregisterConsumer(PubSubConsumer consumer)
    {
        _consumers.forEach((pattern, consumerMap) ->
                           {
                               consumerMap.remove(consumer);
                               if (!consumerMap.isEmpty())
                               {
                                   return;
                               }
                               _consumers.remove(pattern);
                           });
    }

    public void unregisterConsumers()
    {
        _consumers.clear();
    }

}
