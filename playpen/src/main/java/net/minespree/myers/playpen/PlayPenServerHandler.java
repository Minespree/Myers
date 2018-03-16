package net.minespree.myers.playpen;

import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Network;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minespree.myers.common.Server;
import net.minespree.myers.common.manager.RedisServerManager;
import net.minespree.myers.common.manager.ServerHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
public class PlayPenServerHandler implements ServerHandler {
    private final MyersPlugin plugin;

    @Override
    public void onMessage(RedisServerManager.Message message) {
        // Not implemented, as PlayPen handles everything
    }

    @Override
    public void onSync() {
        Map<String, Server> available = plugin.getServerManager().getAllServers();
        List<Server> needToAdd = new ArrayList<>();
        List<Server> needToUpdate = new ArrayList<>();
        List<String> found = new ArrayList<>();

        for (LocalCoordinator coordinator : Network.get().getCoordinators().values()) {
            if (!coordinator.isEnabled())
                continue;

            for (io.playpen.core.coordinator.network.Server server : coordinator.getServers().values()) {
                if (!server.isActive())
                    continue;

                InetSocketAddress address = ServerUtil.getAddress(server);
                if (address.getPort() == 0)
                    continue;

                found.add(server.getName());

                if (!available.containsKey(server.getName())) {
                    Server s = new Server(server.getName(), address, ServerUtil.getPropertiesForMyers(server));
                    needToAdd.add(s);
                } else if (!available.get(server.getName()).getAddress().equals(address)) {
                    Server s = new Server(server.getName(), address, ServerUtil.getPropertiesForMyers(server));
                    needToUpdate.add(s);
                }
            }
        }

        List<String> needToRemove = available.keySet().stream().filter(s -> !found.contains(s)).collect(Collectors.toList());

        for (String s : needToRemove) {
            log.info("Removing server {} as it doesn't exist on the NC", s);
            plugin.getServerManager().deleteServer(s);
        }

        for (Server server : needToUpdate) {
            log.info("Server {} in Redis differs from the NC one, reconciling...", server.getName());
            plugin.getServerManager().deleteServer(server.getName());
            plugin.getServerManager().createServer(server);
        }

        for (Server server : needToAdd) {
            log.info("Server {} missing in Redis, adding it...");
            plugin.getServerManager().createServer(server);
        }
    }
}
