package net.hexuscraft.core.player;

import java.util.Map;
import java.util.UUID;

public record MojangSession(UUID _uuid, String _name, Map<String, String> _properties) {

}
