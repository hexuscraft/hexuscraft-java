package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record ReportSubmittedMessage(UUID reportUUID)
{

    public final static String CHANNEL_NAME = "report.submitted";

    public static ReportSubmittedMessage fromString(final String jsonString)
    {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new ReportSubmittedMessage(UUID.fromString(jsonObject.getString("reportUUID")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.ofEntries(Map.entry("reportUUID", reportUUID.toString()))).toString();
    }

}
