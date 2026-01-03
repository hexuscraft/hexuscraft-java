package net.hexuscraft.common.database;

import redis.clients.jedis.*;
import redis.clients.jedis.mcf.HealthCheckStrategy;
import redis.clients.jedis.mcf.PingStrategy;

import java.time.Duration;

public final class Database {

    public static final String KEY_DELIMITER = ":";

    public final UnifiedJedis _unifiedJedis;

    // TODO::::::::: https://redis.io/docs/latest/develop/clients/jedis/produsage/

    public Database(final String host, final int port, final String username, final String password) throws Exception {
        final HostAndPort hostAndPort = new HostAndPort(host, port);

        final JedisClientConfig clientConfig =
                DefaultJedisClientConfig.builder().user(username).password(password).protocol(RedisProtocol.RESP3)
                        .socketTimeoutMillis(5000).connectionTimeoutMillis(5000).build();

        final ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();
        connectionPoolConfig.setMaxTotal(32);
        connectionPoolConfig.setMaxIdle(connectionPoolConfig.getMaxTotal());
        connectionPoolConfig.setMinIdle(0);
        connectionPoolConfig.setBlockWhenExhausted(true);
        connectionPoolConfig.setMaxWait(Duration.ofSeconds(1));
        connectionPoolConfig.setTestWhileIdle(true);
        connectionPoolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        final HealthCheckStrategy.Config healthCheckStrategyConfig =
                PingStrategy.Config.builder().interval(5000).timeout(3000).numProbes(5).delayInBetweenProbes(100)
                        .build();

        final PingStrategy pingStrategy = new PingStrategy(hostAndPort, clientConfig, healthCheckStrategyConfig);

        final MultiDbConfig.DatabaseConfig multiDbConfigDatabaseConfig =
                MultiDbConfig.DatabaseConfig.builder(hostAndPort, clientConfig)
                        .connectionPoolConfig(connectionPoolConfig).weight(1f).healthCheckStrategy(pingStrategy)
                        .build();

        final MultiDbConfig.CircuitBreakerConfig multiDbConfigCircuitBreakerConfig =
                MultiDbConfig.CircuitBreakerConfig.builder().slidingWindowSize(2).failureRateThreshold(10.0f)
                        .minNumOfFailures(1000).build();

        final MultiDbConfig.RetryConfig multiDbConfigRetryConfig =
                MultiDbConfig.RetryConfig.builder().maxAttempts(3).waitDuration(500).exponentialBackoffMultiplier(2)
                        .build();

        final MultiDbConfig multiDbConfig = MultiDbConfig.builder().database(multiDbConfigDatabaseConfig)
                .failureDetector(multiDbConfigCircuitBreakerConfig).failbackSupported(true)
                .failbackCheckInterval(120000).gracePeriod(60000).commandRetry(multiDbConfigRetryConfig)
                .fastFailover(true).retryOnFailover(false).build();

        final MultiDbClient multiDbClient = MultiDbClient.builder().multiDbConfig(multiDbConfig).build();

//        final CacheConfig cacheConfig = CacheConfig.builder().maxSize(1000).build();
//        _unifiedJedis = new JedisPooled(hostAndPort, clientConfig, cacheConfig, connectionPoolConfig);
//        _unifiedJedis.getPool().preparePool();
//        assert (_unifiedJedis.ping().equals("PONG"));
        _unifiedJedis = multiDbClient;
    }

    public static String buildQuery(final String... args) {
        return String.join(KEY_DELIMITER, args);
    }

}
