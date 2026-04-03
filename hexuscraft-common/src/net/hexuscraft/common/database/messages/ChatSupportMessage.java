package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.enums.PermissionGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public record ChatSupportMessage(UUID _senderUniqueId,
                                 String _senderName,
                                 String _senderServerName,
                                 PermissionGroup[] _senderPermissionGroups,
                                 String _message)
{

    public static String CHANNEL_NAME = "chat.support";

    public static ChatSupportMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray senderPermissionGroups = jsonObject.getJSONArray("senderPermissionGroups");
        return new ChatSupportMessage(UUID.fromString(jsonObject.getString("senderUniqueId")),
                jsonObject.getString("senderName"),
                jsonObject.getString("senderServerName"),
                IntStream.range(0, senderPermissionGroups.length())
                        .mapToObj(senderPermissionGroups::getString)
                        .map(PermissionGroup::valueOfSafe)
                        .toArray(PermissionGroup[]::new),
                jsonObject.getString("message"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.ofEntries(Map.entry("senderUniqueId", _senderUniqueId.toString()),
                Map.entry("senderName", _senderName),
                Map.entry("senderServerName", _senderServerName),
                Map.entry("senderPermissionGroups", new JSONArray(_senderPermissionGroups)),
                Map.entry("message", _message))).toString();
    }

}
