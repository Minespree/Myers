package net.minespree.myers.bungee;

import net.minespree.myers.common.Server;
import net.minespree.myers.common.manager.RedisServerManager;
import net.minespree.myers.common.manager.ServerHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class BungeeCordServerHandler implements ServerHandler {
    private static final BaseComponent[] DISCONNECTED =
            new ComponentBuilder("We couldn't connect you to a lobby, so you have been disconnected")
                    .color(ChatColor.RED)
                    .create();
    private static final BaseComponent[] CONNECTED_TO_LOBBY =
            new ComponentBuilder("The server you were on is down, you have been moved to the lobby")
                    .color(ChatColor.RED)
                    .create();

    private final Set<String> original;
    private final MyersPlugin plugin;

    public BungeeCordServerHandler(MyersPlugin plugin) {
        this.original = new HashSet<>(ProxyServer.getInstance().getServers().keySet());
        this.plugin = plugin;
    }

    @Override
    public void onMessage(RedisServerManager.Message message) {
        switch (message.getType()) {
            case CREATE:
                ServerInfo info = ProxyServer.getInstance().constructServerInfo(message.getServer().getName(),
                        message.getServer().getAddress(), "myers server", false);
                ProxyServer.getInstance().getServers().put(message.getServer().getName(), info);
                plugin.getLogger().info("Created lobby: " + message.getServer());
                break;
            case DESTROY:
                ServerInfo info1 = ProxyServer.getInstance().getServerInfo(message.getServer().getName());
                if (info1 == null)
                    return;

                ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
                    try {
                        disconnectPlayers(info1);
                    } catch (InterruptedException e) {
                        plugin.getLogger().log(Level.INFO, "Got interrupted whilst disconnecting players from " + info1.getName(), e);
                    }

                    ProxyServer.getInstance().getServers().remove(info1.getName());
                    plugin.getLogger().info("Destroyed lobby: " + message.getServer());
                });
                break;
        }
    }

    @Override
    public void onSync() {
        Map<String, ServerInfo> managedServers = new HashMap<>(ProxyServer.getInstance().getServers());
        Map<String, Server> myersServers = plugin.getServerManager().getAllServers();
        managedServers.keySet().removeAll(original);

        for (Map.Entry<String, Server> entry : myersServers.entrySet()) {
            if (!managedServers.containsKey(entry.getKey())) {
                // Create server
                ServerInfo info = ProxyServer.getInstance().constructServerInfo(entry.getKey(), entry.getValue().getAddress(), "myers server", false);
                ProxyServer.getInstance().getServers().put(entry.getKey(), info);
                plugin.getLogger().info("Created server from resync: " + entry.getValue());
            }
        }

        for (Map.Entry<String, ServerInfo> entry : managedServers.entrySet()) {
            if (!myersServers.containsKey(entry.getKey())) {
                try {
                    disconnectPlayers(entry.getValue());
                } catch (InterruptedException e) {
                    plugin.getLogger().log(Level.INFO, "Got interrupted whilst disconnecting players from " + entry.getKey(), e);
                }
                ProxyServer.getInstance().getServers().remove(entry.getKey());
                plugin.getLogger().info("Deleted server from resync: " + entry.getValue());
            }
        }
    }

    private void disconnectPlayers(ServerInfo server) throws InterruptedException {
        // This will block until all players are disconnected.
        Collection<ProxiedPlayer> players = new HashSet<>(server.getPlayers());
        if (players.isEmpty())
            return;

        CountDownLatch latch = new CountDownLatch(players.size());

        ServerInfo lobbyServer = ProxyServer.getInstance().getServerInfo("Lobby");
        for (ProxiedPlayer player : players) {
            player.connect(lobbyServer, (ignored, exception) -> {
                if (exception != null) {
                    player.disconnect(DISCONNECTED);
                } else {
                    player.sendMessage(CONNECTED_TO_LOBBY);
                }
                latch.countDown();
            });
        }

        latch.await();
    }
}
