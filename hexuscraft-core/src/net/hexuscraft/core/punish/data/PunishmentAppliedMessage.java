package net.hexuscraft.core.punish.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.UUID;

public record PunishmentAppliedMessage(UUID _targetUniqueId, UUID _punishmentId) {

    public final static String CHANNEL_NAME = "punish.punishmentApplied";

    public String toJsonString() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("targetUniqueId", _targetUniqueId.toString());
        jsonObject.addProperty("punishmentId", _punishmentId.toString());
        return new Gson().toJson(jsonObject);
    }

    public static PunishmentAppliedMessage fromJsonString(final String jsonString) {
        final JsonParser jsonParser = new JsonParser();
        final JsonObject jsonObject = jsonParser.parse(jsonString).getAsJsonObject();
        return new PunishmentAppliedMessage(UUID.fromString(jsonObject.get("targetUniqueId").getAsString()), UUID.fromString(jsonObject.get("punishmentId").getAsString()));
    }

}
