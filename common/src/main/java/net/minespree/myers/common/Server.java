package net.minespree.myers.common;

import lombok.Value;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;

@Value
public class Server {
    private final String name;
    private final InetSocketAddress address;
    private final Map<String, String> properties;

    public boolean isGame() {
        return properties.getOrDefault("is_game", "false").equals("true");
    }
}
