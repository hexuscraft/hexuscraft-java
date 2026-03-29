package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.enums.PunishType;
import org.json.JSONObject;

import java.util.UUID;

public class PunishPunishmentAppliedMessage
{

    public static String CHANNEL_NAME = "punish.punishmentApplied";

    public PunishData _punishData;

    public PunishPunishmentAppliedMessage(PunishData punishData)
    {
        _punishData = punishData;
    }

    public static PunishPunishmentAppliedMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishPunishmentAppliedMessage(new PunishData(UUID.fromString(jsonObject.getString("id")),
                jsonObject.getEnum(PunishType.class, "type"),
                jsonObject.getBoolean("active"),
                jsonObject.getLong("origin"),
                jsonObject.getLong("length"),
                jsonObject.getString("reason"),
                UUID.fromString(jsonObject.getString("targetUUID")),
                jsonObject.getString("targetServer"),
                UUID.fromString(jsonObject.getString("staffUUID")),
                jsonObject.getString("staffServer"),
                jsonObject.optLongObject("removeOrigin"),
                jsonObject.optString("removeReason"),
                jsonObject.optString("removeServer"),
                jsonObject.has("removeStaffUUID") ? UUID.fromString(jsonObject.getString("removeStaffUUID")) : null,
                jsonObject.optString("removeStaffServer")));
    }

    @Override
    public String toString()
    {
        return new JSONObject(_punishData).toString();
    }

}
