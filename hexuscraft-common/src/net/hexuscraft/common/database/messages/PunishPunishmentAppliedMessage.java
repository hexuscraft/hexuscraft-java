package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class PunishPunishmentAppliedMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "punish.punishmentApplied";

    public final UUID _targetUUID;
    public final UUID _punishmentUUID;

    public PunishPunishmentAppliedMessage(final UUID targetUUID, final UUID punishmentUUID) {
        _targetUUID = targetUUID;
        _punishmentUUID = punishmentUUID;
    }

    public static PunishPunishmentAppliedMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishPunishmentAppliedMessage(UUID.fromString(jsonObject.getString("targetUUID")),
                UUID.fromString(jsonObject.getString("punishmentUUID")));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID",
                        _targetUUID.toString()),
                Map.entry("punishmentUUID",
                        _punishmentUUID.toString()))).toString();
    }

}
