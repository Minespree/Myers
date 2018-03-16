package net.minespree.myers.common.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class InetSocketAddressSerializer implements JsonSerializer<InetSocketAddress>, JsonDeserializer<InetSocketAddress> {
    @Override
    public InetSocketAddress deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        InetAddress address = context.deserialize(object.get("host"), InetAddress.class);
        int port = object.get("port").getAsInt();
        return new InetSocketAddress(address, port);
    }

    @Override
    public JsonElement serialize(InetSocketAddress src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("host", context.serialize(src.getAddress()));
        object.addProperty("port", src.getPort());
        return context.serialize(object);
    }
}
