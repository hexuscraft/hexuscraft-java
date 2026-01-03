package net.hexuscraft.common.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record PunishmentAppliedMessage(UUID _targetUniqueId, UUID _punishmentId) {

    public final static String CHANNEL_NAME = "punish.punishmentApplied";

    public static PunishmentAppliedMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishmentAppliedMessage(UUID.fromString(jsonObject.getString("targetUniqueId")),
                UUID.fromString(jsonObject.getString("punishmentId")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.of("targetUniqueId", _targetUniqueId.toString(), "punishmentId",
                _punishmentId.toString())).toString();
    }

}
