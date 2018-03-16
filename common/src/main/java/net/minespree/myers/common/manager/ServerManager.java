package net.minespree.myers.common.manager;

import net.minespree.myers.common.Server;

import java.util.Map;

public interface ServerManager {
    void createServer(Server server);
    void deleteServer(String server);
    Map<String, Server> getAllServers();
    void synchronizeServers();
    void synchronizeServers(boolean callSync);
}
