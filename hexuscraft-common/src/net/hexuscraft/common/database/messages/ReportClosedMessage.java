package net.hexuscraft.common.database.messages;

import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record ReportClosedMessage(UUID reportUUID) {

    public final static String CHANNEL_NAME = "report.closed";

    public static ReportClosedMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        return new ReportClosedMessage(UUID.fromString(jsonObject.getString("reportUUID")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(Map.ofEntries(Map.entry("reportUUID", reportUUID.toString()))).toString();
    }

}
