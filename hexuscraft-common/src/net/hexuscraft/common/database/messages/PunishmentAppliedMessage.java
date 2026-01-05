package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public final class PunishmentAppliedMessage extends BaseMessage {

    public final static String CHANNEL_NAME = "punish.punishmentApplied";

    public final UUID _targetUUID;
    public final UUID _punishmentUUID;

    public PunishmentAppliedMessage(final UUID targetUUID, final UUID punishmentUUID) {
        this._targetUUID = targetUUID;
        this._punishmentUUID = punishmentUUID;
    }

    public static PunishmentAppliedMessage parse(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishmentAppliedMessage(UUID.fromString(jsonObject.getString("targetUUID")), UUID.fromString(jsonObject.getString("punishmentUUID")));
    }

    public String stringify() {
        return new JSONObject(Map.ofEntries(Map.entry("targetUUID", _targetUUID.toString()), Map.entry("punishmentUUID", _punishmentUUID.toString()))).toString();
    }

}
