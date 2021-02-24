package ovh.roro.wankil.deathswap.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.bukkit.Location;
import ovh.roro.wankil.deathswap.DeathSwap;

/**
 * This file is a part of TheWalls project.
 *
 * @author roro1506_HD
 */
public class LocationTypeAdapter implements JsonDeserializer<Location> {

    private final DeathSwap plugin;

    public LocationTypeAdapter(DeathSwap plugin) {
        this.plugin = plugin;
    }

    @Override
    public Location deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        return new Location(
                object.has("world") ? this.plugin.getServer().getWorld(object.get("world").getAsString()) : null,
                object.get("x").getAsDouble(),
                object.get("y").getAsDouble(),
                object.get("z").getAsDouble(),
                object.has("yaw") ? object.get("yaw").getAsFloat() : 0.0F,
                object.has("pitch") ? object.get("pitch").getAsFloat() : 0.0F
        );
    }
}
