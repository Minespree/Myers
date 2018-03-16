package net.minespree.myers.playpen;

import com.google.common.collect.ImmutableMap;
import io.playpen.core.coordinator.network.Server;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;

@UtilityClass
public class ServerUtil {
    public static InetSocketAddress getAddress(Server server) {
        InetAddress address = ((InetSocketAddress) server.getCoordinator().getChannel().remoteAddress()).getAddress();
        int port = Integer.parseInt(server.getProperties().getOrDefault("port", "0"));
        return new InetSocketAddress(address, port);
    }

    public static Map<String, String> getPropertiesForMyers(Server server) {
        ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
        b.put("playpen_package", server.getP3().getId());
        for (Map.Entry<String, String> entry : server.getProperties().entrySet()) {
            if (entry.getKey().startsWith("myers_") && !entry.getKey().equals("myers_expose")) {
                b = b.put(entry.getKey().substring(6), entry.getValue());
            }
        }
        return b.build();
    }
}
