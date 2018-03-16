package net.minespree.myers.common.manager;

public interface ServerHandler {
    void onMessage(RedisServerManager.Message message);
    void onSync();
}
