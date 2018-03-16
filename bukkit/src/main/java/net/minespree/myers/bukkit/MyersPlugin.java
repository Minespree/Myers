package net.minespree.myers.bukkit;

import lombok.Getter;
import net.minespree.myers.common.manager.RedisServerManager;
import net.minespree.myers.common.manager.ServerManager;
import net.minespree.myers.common.util.MyersPubSubListener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MyersPlugin extends JavaPlugin {
    @Getter
    private ServerManager serverManager;
    private MyersPubSubListener pubSubListener;
    private JedisPool jedisPool;
    @Getter
    public static MyersPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        jedisPool = createJedisPool();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }

        getLogger().info("Established Redis connection!");

        serverManager = new RedisServerManager(jedisPool, new BukkitServerHandler());

        serverManager.synchronizeServers();
        getServer().getScheduler().runTaskTimerAsynchronously(this, serverManager::synchronizeServers, 30 * 20, 90 * 20);

        pubSubListener = new MyersPubSubListener(getConfig().getString("redis-host"), getConfig().getString("redis-password", null), getConfig().getInt("redis-port"), (RedisServerManager) serverManager);
        getServer().getScheduler().runTaskAsynchronously(this, pubSubListener);
    }

    @Override
    public void onDisable() {
        jedisPool.destroy();
        pubSubListener.getRunning().set(false);
        if (pubSubListener.getThread() != null) {
            pubSubListener.getThread().interrupt();
        }
    }

    private JedisPool createJedisPool() {
        // Set context class loader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MyersPlugin.class.getClassLoader());

        try {
            return new JedisPool(new JedisPoolConfig(), getConfig().getString("redis-host"), getConfig().getInt("redis-port"), 500, getConfig().getString("redis-password", null));
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }
}
