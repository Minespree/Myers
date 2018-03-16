package net.minespree.myers.common.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.minespree.myers.common.Server;
import net.minespree.myers.common.util.InetSocketAddressSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class RedisServerManager implements ServerManager {
    private final Map<String, Server> servers = new HashMap<>();
    private final UUID source = UUID.randomUUID();
    private final Lock lock = new ReentrantLock();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressSerializer())
            .create();
    private final JedisPool jedisPool;
    private final ServerHandler handler;

    @Override
    public void createServer(Server server) {
        Objects.requireNonNull(server, "server");

        lock.lock();
        try {
            if (servers.putIfAbsent(server.getName(), server) != null) {
                throw new IllegalArgumentException("Server already exists");
            }
        } finally {
            lock.unlock();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("myers:servers", server.getName(), gson.toJson(server));
            jedis.publish("myers-updates", gson.toJson(new Message(MessageType.CREATE, source, server)));
        }
    }

    @Override
    public void deleteServer(String server) {
        Objects.requireNonNull(server, "server");

        lock.lock();
        try {
            if (servers.remove(server) == null) {
                return;
            }
        } finally {
            lock.unlock();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("myers:servers", server);
            jedis.publish("myers-updates", gson.toJson(new Message(MessageType.DESTROY, source, new Server(server, null, null))));
        }
    }

    @Override
    public Map<String, Server> getAllServers() {
        lock.lock();
        try {
            return Collections.unmodifiableMap(new HashMap<>(servers));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void synchronizeServers() {
        synchronizeServers(true);
    }

    @Override
    public void synchronizeServers(boolean callSync) {
        Map<String, Server> all = new HashMap<>();

        Map<String, String> asJson;
        try (Jedis jedis = jedisPool.getResource()) {
            asJson = jedis.hgetAll("myers:servers");
        }

        for (Map.Entry<String, String> entry : asJson.entrySet()) {
            all.put(entry.getKey(), gson.fromJson(entry.getValue(), Server.class));
        }

        lock.lock();
        try {
            servers.clear();
            servers.putAll(all);
        } finally {
            lock.unlock();
        }

        if (callSync) {
            handler.onSync();
        }
    }

    public JedisPubSub getPubSubHandler() {
        return new PubSubHandler();
    }

    public class PubSubHandler extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
            if (channel.equals("myers-update")) {
                Message message1 = gson.fromJson(message, Message.class);
                if (message1.source.equals(source))
                    return;

                switch (message1.type) {
                    case CREATE:
                        createServer(message1.server);
                        break;
                    case DESTROY:
                        deleteServer(message1.server.getName());
                        break;
                }
                handler.onMessage(message1);
            }
        }
    }

    @Value
    public static class Message {
        private final MessageType type;
        private final UUID source;
        private final Server server;
    }

    public enum MessageType {
        CREATE,
        DESTROY
    }
}
