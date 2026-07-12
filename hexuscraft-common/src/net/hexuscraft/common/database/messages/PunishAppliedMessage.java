package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishAppliedMessage {

	public static String CHANNEL_NAME = "punish.applied";

	public UUID _uuid;

	public PunishAppliedMessage(UUID uuid) {
		_uuid = uuid;
	}

	public static PunishAppliedMessage fromString(String jsonString) {
		return new PunishAppliedMessage(UUID.fromString(new JSONObject(jsonString).getString("uuid")));
	}

	@Override
	public String toString() {
		return new JSONObject(new HashMap<>(Map.ofEntries(Map.entry("uuid", _uuid)))).toString();
	}

}
