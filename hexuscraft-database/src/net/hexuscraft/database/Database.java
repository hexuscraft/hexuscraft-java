package net.hexuscraft.database;

import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.util.Pool;

public final class Database {

    public static final String KEY_DELIMITER = ":";

    public final JedisPooled _jedisPooled;

    public Database(final String host, final int port, final String username, final String password) throws Exception {
        _jedisPooled = new JedisPooled(host, port, username, password);

        final Pool<Connection> pool = _jedisPooled.getPool();
        pool.preparePool();
        assert (_jedisPooled.ping().equals("PONG"));
    }

    public static String buildQuery(final String... args) {
        return String.join(KEY_DELIMITER, args);
    }

}
