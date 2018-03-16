package net.minespree.myers.bungee;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.minespree.myers.common.manager.RedisServerManager;
import net.minespree.myers.common.manager.ServerManager;
import net.minespree.myers.common.util.MyersPubSubListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.concurrent.*;

public class MyersPlugin extends Plugin {
    @Getter
    private ServerManager serverManager;
    private MyersPubSubListener pubSubListener;
    private JedisPool jedisPool;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");
        Configuration configuration;

        try {
            if (!file.exists()) {
                file.createNewFile();
                try (InputStream in = getResourceAsStream("example_config.yml");
                     OutputStream out = new FileOutputStream(file)) {
                    ByteStreams.copy(in, out);
                }
            }

            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save configuration", e);

        }
        jedisPool = createJedisPool(configuration);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }

        getLogger().info("Established Redis connection!");

        serverManager = new RedisServerManager(jedisPool, new BungeeCordServerHandler(this));

        serverManager.synchronizeServers();
        getProxy().getScheduler().schedule(this, serverManager::synchronizeServers, 30, 90, TimeUnit.SECONDS);

        pubSubListener = new MyersPubSubListener(configuration.getString("redis-host"),
                configuration.getString("redis-password", null), configuration.getInt("redis-port", 6379),
                (RedisServerManager) serverManager);
        getProxy().getScheduler().runAsync(this, pubSubListener);
    }

    @Override
    public void onDisable() {
        jedisPool.destroy();
        pubSubListener.getRunning().set(false);
        if (pubSubListener.getThread() != null) {
            pubSubListener.getThread().interrupt();
        }
    }

    private JedisPool createJedisPool(Configuration configuration) {
        FutureTask<JedisPool> poolFuture = new FutureTask<>(() -> {
            // Set context class loader
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(MyersPlugin.class.getClassLoader());

            try {
                return new JedisPool(new JedisPoolConfig(), configuration.getString("redis-host"), configuration.getInt("redis-port"), 500, configuration.getString("redis-password", null));
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        });
        getProxy().getScheduler().runAsync(this, poolFuture);
        try {
            return poolFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("unable to create Redis pool", e);
        }
    }
}
