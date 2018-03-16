package net.minespree.myers.playpen;

import io.playpen.core.coordinator.network.INetworkListener;
import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Server;
import io.playpen.core.plugin.EventManager;
import io.playpen.core.plugin.IPlugin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MyersNetworkListener implements INetworkListener {
    private final MyersPlugin plugin;

    @Override
    public void onNetworkStartup() {

    }

    @Override
    public void onNetworkShutdown() {

    }

    @Override
    public void onCoordinatorCreated(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onCoordinatorSync(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onRequestProvision(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onProvisionResponse(LocalCoordinator lc, Server server, boolean ok) {
        if (!ok) {
            return;
        }

        if (server.getProperties().getOrDefault("myers_expose", "true").equals("false")) {
            return;
        }

        net.minespree.myers.common.Server s = new net.minespree.myers.common.Server(server.getName(), ServerUtil.getAddress(server),
                ServerUtil.getPropertiesForMyers(server));
        if (!plugin.getServerManager().getAllServers().containsKey(s.getName())) {
            plugin.getServerManager().createServer(s);
        }
    }

    @Override
    public void onRequestDeprovision(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onServerShutdown(LocalCoordinator localCoordinator, Server server) {
        plugin.getServerManager().deleteServer(server.getName());
    }

    @Override
    public void onRequestShutdown(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onPluginMessage(IPlugin iPlugin, String s, Object... objects) {

    }

    @Override
    public void onListenerRegistered(EventManager<INetworkListener> eventManager) {

    }

    @Override
    public void onListenerRemoved(EventManager<INetworkListener> eventManager) {

    }
}
