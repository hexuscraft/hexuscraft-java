package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record PunishPunishmentAppliedMessage(UUID _targetUUID, UUID _punishmentUUID) {

    public final static String CHANNEL_NAME = "punish.punishmentApplied";

    public static PunishPunishmentAppliedMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishPunishmentAppliedMessage(UUID.fromString(jsonObject.getString("targetUUID")),
                UUID.fromString(jsonObject.getString("punishmentUUID")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()),
                Map.entry("punishmentUUID", _punishmentUUID.toString()))).toString();
    }

}
