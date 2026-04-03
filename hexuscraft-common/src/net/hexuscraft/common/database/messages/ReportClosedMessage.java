package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record ReportClosedMessage(UUID uuid)
{

    public static String CHANNEL_NAME = "report.closed";

    public static ReportClosedMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new ReportClosedMessage(UUID.fromString(jsonObject.getString("uuid")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.ofEntries(Map.entry("uuid", uuid.toString()))).toString();
    }

}
