package net.hexuscraft.database;

import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.util.Pool;

public class Database {

    public static final String KEY_DELIMITER = ":";

    public final JedisPooled _jedisPooled;

    public Database(String host, int port) throws Exception {
        _jedisPooled = new JedisPooled(host, port);
        Pool<Connection> pool = _jedisPooled.getPool();
        pool.preparePool();
        assert (_jedisPooled.ping().equals("PONG"));
    }

    public static String buildQuery(String... args) {
        return String.join(KEY_DELIMITER, args);
    }

}
