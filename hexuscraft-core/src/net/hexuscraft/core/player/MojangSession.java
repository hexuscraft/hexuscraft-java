package net.hexuscraft.core.player;

import java.util.HashMap;
import java.util.UUID;

public class MojangSession {

    public final UUID uuid;
    public final String name;

    public final HashMap<String, String> properties;

    MojangSession(UUID uuid, String name, HashMap<String, String> properties) {
        this.uuid = uuid;
        this.name = name;
        this.properties = properties;
    }

}
