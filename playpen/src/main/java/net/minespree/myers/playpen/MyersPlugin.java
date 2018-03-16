package net.minespree.myers.playpen;

import io.playpen.core.coordinator.network.Network;
import io.playpen.core.plugin.AbstractPlugin;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minespree.myers.common.manager.RedisServerManager;
import net.minespree.myers.common.manager.ServerManager;
import net.minespree.myers.common.util.MyersPubSubListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

@Log4j2
public class MyersPlugin extends AbstractPlugin {
    @Getter
    private ServerManager serverManager;
    private MyersPubSubListener pubSubListener;
    private JedisPool jedisPool;

    @Override
    public boolean onStart() {
        log.info("Myers is connecting to Redis...");

        jedisPool = createJedisPool();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }

        log.info("Established Redis connection!");

        serverManager = new RedisServerManager(jedisPool, new PlayPenServerHandler(this));

        Network.get().getScheduler().scheduleAtFixedRate(serverManager::synchronizeServers, 30, 90, TimeUnit.SECONDS);
        Network.get().getEventManager().registerListener(new MyersNetworkListener(this));

        pubSubListener = new MyersPubSubListener(getConfig().getString("redis-host"),
                getConfig().optString("redis-password", null),
                getConfig().optInt("redis-port", 6379), (RedisServerManager) serverManager);
        new Thread(pubSubListener).start();

        return true;
    }

    @Override
    public void onStop() {
        jedisPool.destroy();
        if (pubSubListener != null) {
            pubSubListener.getRunning().set(false);
            if (pubSubListener.getThread() != null) {
                pubSubListener.getThread().interrupt();
            }
        }
    }

    private JedisPool createJedisPool() {
        // Set context class loader
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MyersPlugin.class.getClassLoader());

        try {
            return new JedisPool(new JedisPoolConfig(), getConfig().getString("redis-host"),
                    getConfig().optInt("redis-port", 6379), 500,
                    getConfig().optString("redis-password", null));
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }
}
