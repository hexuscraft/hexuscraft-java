package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalTeleportMessage extends BaseMessage {

	public static String CHANNEL_NAME = "portal.teleport";

	public UUID _targetUUID;
	public String _serverName;
	public UUID _senderUUID;

	public PortalTeleportMessage(UUID targetUUID, String serverName, UUID senderUUID) {
		_targetUUID = targetUUID;
		_serverName = serverName;
		_senderUUID = senderUUID;
	}

	public static PortalTeleportMessage parse(String jsonString) {
		JSONObject jsonObject = new JSONObject(jsonString);
		return new PortalTeleportMessage(UUID.fromString(jsonObject.getString("targetUUID")), jsonObject.getString("serverName"), jsonObject.has("senderUUID") ? UUID.fromString(jsonObject.getString("senderUUID")) : null);
	}

	public String stringify() {
		Map<String, String> data = new HashMap<>(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()), Map.entry("serverName", _serverName)));
		if (_senderUUID != null) data.put("senderUUID", _senderUUID.toString());
		return new JSONObject(data).toString();
	}

}
