package com.stack3mpty.crawler.server.dao.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPoolConfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class TaskJedisPools {

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int timeout = 2000;
    private int database = 0;

    private Jedis jedis = new Jedis();

    private List<RedisConnectionFactory> connectionFactories;
    private List<RedisTemplate<String, Object>> redisTemplates;
    private final AtomicInteger counter = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        log.info("初始化 Redis 连接池，数量: {}", jedis.pool.size);

        connectionFactories = new ArrayList<>(jedis.pool.size);
        redisTemplates = new ArrayList<>(jedis.pool.size);

        for (int i = 0; i < jedis.pool.size; i++) {
            RedisConnectionFactory factory = createConnectionFactory();
            RedisTemplate<String, Object> template = createRedisTemplate(factory);

            connectionFactories.add(factory);
            redisTemplates.add(template);

            log.debug("创建第 {} 个 Redis 连接", i + 1);
        }

        log.info("Redis 连接池初始化完成");
    }

    private RedisConnectionFactory createConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);

        if (password != null && !password.trim().isEmpty()) {
            config.setPassword(password);
        }

        JedisConnectionFactory factory = getJedisConnectionFactory(config);
        factory.afterPropertiesSet();

        return factory;
    }

    private JedisConnectionFactory getJedisConnectionFactory(RedisStandaloneConfiguration config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(jedis.pool.maxActive);
        poolConfig.setMaxIdle(jedis.pool.maxIdle);
        poolConfig.setMinIdle(jedis.pool.minIdle);
        poolConfig.setMaxWaitMillis(jedis.pool.maxWait);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setPoolConfig(poolConfig);
        factory.setTimeout(timeout);
        return factory;
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 获取连接池数量
     */
    public int getPoolNum() {
        return jedis.pool.size;
    }

    /**
     * 根据轮询策略获取 RedisTemplate
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        validateTemplates();
        int index = counter.getAndIncrement() % jedis.pool.size;
        return redisTemplates.get(index);
    }

    /**
     * 根据 key 的 hash 值获取 RedisTemplate
     */
    public RedisTemplate<String, Object> getRedisTemplate(String key) {
        validateTemplates();
        int index = Math.abs(key.hashCode()) % jedis.pool.size;
        return redisTemplates.get(index);
    }

    /**
     * 根据索引获取 RedisTemplate
     */
    public RedisTemplate<String, Object> getRedisTemplate(int index) {
        validateTemplates();
        if (index < 0 || index >= jedis.pool.size) {
            throw new IllegalArgumentException("Index out of range: " + index);
        }
        return redisTemplates.get(index);
    }

    /**
     * 根据轮询策略获取 RedisConnectionFactory
     */
    public RedisConnectionFactory getConnectionFactory() {
        validateFactories();
        int index = counter.get() % jedis.pool.size;
        return connectionFactories.get(index);
    }

    /**
     * 根据索引获取 RedisConnectionFactory
     */
    public RedisConnectionFactory getConnectionFactory(int index) {
        validateFactories();
        if (index < 0 || index >= jedis.pool.size) {
            throw new IllegalArgumentException("Index out of range: " + index);
        }
        return connectionFactories.get(index);
    }

    private void validateTemplates() {
        if (redisTemplates == null || redisTemplates.isEmpty()) {
            throw new IllegalStateException("RedisTemplates not initialized");
        }
    }

    private void validateFactories() {
        if (connectionFactories == null || connectionFactories.isEmpty()) {
            throw new IllegalStateException("ConnectionFactories not initialized");
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁 Redis 连接池");

        if (connectionFactories != null) {
            connectionFactories.forEach(factory -> {
                if (factory instanceof JedisConnectionFactory) {
                    ((JedisConnectionFactory) factory).destroy();
                }
            });
            connectionFactories.clear();
        }

        if (redisTemplates != null) {
            redisTemplates.clear();
        }

        log.info("Redis 连接池销毁完成");
    }

    // 内部配置类
    public static class Jedis {
        private Pool pool = new Pool();

        public Pool getPool() { return pool; }
        public void setPool(Pool pool) { this.pool = pool; }

        public static class Pool {
            private int size = 3;
            private int maxActive = 8;
            private int maxIdle = 8;
            private int minIdle = 0;
            private long maxWait = 3000;

            // getters and setters
            public int getSize() { return size; }
            public void setSize(int size) { this.size = size; }

            public int getMaxActive() { return maxActive; }
            public void setMaxActive(int maxActive) { this.maxActive = maxActive; }

            public int getMaxIdle() { return maxIdle; }
            public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }

            public int getMinIdle() { return minIdle; }
            public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

            public long getMaxWait() { return maxWait; }
            public void setMaxWait(long maxWait) { this.maxWait = maxWait; }
        }
    }

    // getters and setters
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getDatabase() { return database; }
    public void setDatabase(int database) { this.database = database; }

    public Jedis getJedis() { return jedis; }
    public void setJedis(Jedis jedis) { this.jedis = jedis; }
}