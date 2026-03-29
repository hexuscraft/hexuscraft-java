package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.enums.PermissionGroup;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public record ChatSupportMessage(UUID _senderUniqueId,
                                 String _username,
                                 PermissionGroup[] _permissionGroups,
                                 String _serverName,
                                 String _message)
{

    public static String CHANNEL_NAME = "chat.support";

    public static ChatSupportMessage fromString(String jsonString)
    {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray permissionGroups = jsonObject.getJSONArray("permissionGroups");
        return new ChatSupportMessage(UUID.fromString(jsonObject.getString("senderUniqueId")),
                                      jsonObject.getString("username"),
                                      IntStream.range(0, permissionGroups.length())
                                               .mapToObj(permissionGroups::getString)
                                               .map(PermissionGroup::valueOfSafe)
                                               .toArray(PermissionGroup[]::new),
                                      jsonObject.getString("serverName"),
                                      jsonObject.getString("message"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.ofEntries(Map.entry("senderUniqueId", _senderUniqueId.toString()),
                                            Map.entry("username", _username),
                                            Map.entry("permissionGroups", new JSONArray(_permissionGroups)),
                                            Map.entry("serverName", _serverName),
                                            Map.entry("message", _message))).toString();
    }

}
