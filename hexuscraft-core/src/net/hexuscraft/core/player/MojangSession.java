package net.hexuscraft.core.player;

import java.util.HashMap;
import java.util.UUID;

public record MojangSession(UUID _uuid, String _name, HashMap<String, String> _properties) {

}
