package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.enums.PermissionGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public record ChatSupportResponseSentMessage(UUID _uuid,
                                             UUID _senderUniqueId,
                                             String _senderName,
                                             String _senderServerName,
                                             PermissionGroup[] _senderPermissionGroups,
                                             UUID _targetUniqueId,
                                             String _targetName,
                                             String _message)
{

    public static String CHANNEL_NAME = "chat.support.response.sent";

    public static ChatSupportResponseSentMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray senderPermissionGroups = jsonObject.getJSONArray("senderPermissionGroups");
        return new ChatSupportResponseSentMessage(UUID.fromString(jsonObject.getString("uuid")),
                UUID.fromString(jsonObject.getString("senderUniqueId")),
                jsonObject.getString("senderName"),
                jsonObject.getString("senderServerName"),
                IntStream.range(0, senderPermissionGroups.length())
                        .mapToObj(senderPermissionGroups::getString)
                        .map(PermissionGroup::valueOfSafe)
                        .toArray(PermissionGroup[]::new),
                UUID.fromString(jsonObject.getString("targetUniqueId")),
                jsonObject.getString("targetName"),
                jsonObject.getString("message"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.ofEntries(Map.entry("uuid", _uuid.toString()),
                Map.entry("senderUniqueId", _senderUniqueId.toString()),
                Map.entry("senderName", _senderName),
                Map.entry("senderServerName", _senderServerName),
                Map.entry("senderPermissionGroups", new JSONArray(_senderPermissionGroups)),
                Map.entry("targetUniqueId", _targetUniqueId.toString()),
                Map.entry("targetName", _targetName),
                Map.entry("message", _message))).toString();
    }

}
