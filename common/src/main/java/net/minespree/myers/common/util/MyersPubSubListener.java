package net.minespree.myers.common.util;

import net.minespree.myers.common.manager.RedisServerManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class MyersPubSubListener implements Runnable {
    private final String redisAddress;
    private final String redisPassword;
    private final int redisPort;
    private final RedisServerManager serverManager;
    @Getter
    private final AtomicBoolean running = new AtomicBoolean(true);
    @Getter
    private Thread thread;
    private int tries = 0;

    @Override
    public void run() {
        thread = Thread.currentThread();
        while (running.get()) {
            try (Jedis jedis = new Jedis(redisAddress, redisPort, 500)) {
                if (redisPassword != null) {
                    jedis.auth(redisPassword);
                }

                jedis.ping();
                tries = 0;
                jedis.subscribe(serverManager.getPubSubHandler(), "myers-updates");
            } catch (JedisConnectionException e) {
                // try again in a bit
                tries++;
                try {
                    Thread.sleep(500 * tries);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
        thread = null;
    }
}
