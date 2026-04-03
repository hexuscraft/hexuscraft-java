package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.enums.PunishType;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishAppliedMessage
{

    public static String CHANNEL_NAME = "punish.applied";

    public PunishData _punishData;

    public PunishAppliedMessage(PunishData punishData)
    {
        _punishData = punishData;
    }

    public static PunishAppliedMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new PunishAppliedMessage(new PunishData(UUID.fromString(jsonObject.getString("uuid")),
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
                jsonObject.optString("removeTargetServer"),
                jsonObject.has("removeStaffUUID") ? UUID.fromString(jsonObject.getString("removeStaffUUID")) : null,
                jsonObject.optString("removeStaffServer")));
    }

    @Override
    public String toString()
    {
        Map<Object, Object> data = new HashMap<>(Map.ofEntries(Map.entry("uuid", _punishData._uuid),
                Map.entry("type", _punishData._type),
                Map.entry("active", _punishData._active),
                Map.entry("origin", _punishData._origin),
                Map.entry("length", _punishData._length),
                Map.entry("reason", _punishData._reason),
                Map.entry("targetUUID", _punishData._targetUUID),
                Map.entry("targetServer", _punishData._targetServer),
                Map.entry("staffUUID", _punishData._staffUUID),
                Map.entry("staffServer", _punishData._staffServer)));

        if (!_punishData._active)
        {
            data.putAll(Map.ofEntries(Map.entry("removeOrigin", _punishData._removeOrigin),
                    Map.entry("removeReason", _punishData._removeReason),
                    Map.entry("removeTargetServer", _punishData._removeTargetServer),
                    Map.entry("removeStaffUUID", _punishData._removeStaffUUID),
                    Map.entry("removeStaffServer", _punishData._removeStaffServer)));
        }

        return new JSONObject(data).toString();
    }

}
