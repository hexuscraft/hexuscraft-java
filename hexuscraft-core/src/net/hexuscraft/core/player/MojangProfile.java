package net.hexuscraft.core.player;

import java.util.UUID;

public class MojangProfile {
    public final UUID uuid;
    public final String name;

    MojangProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
