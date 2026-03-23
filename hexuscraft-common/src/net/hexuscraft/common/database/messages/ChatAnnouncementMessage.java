package net.hexuscraft.common.database.messages;

import net.hexuscraft.common.enums.PermissionGroup;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public record ChatAnnouncementMessage(UUID _senderUniqueId, String _message, PermissionGroup _permissionGroup) {

    public final static String CHANNEL_NAME = "chat.announcement";

    public static ChatAnnouncementMessage fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);

        return new ChatAnnouncementMessage(UUID.fromString(jsonObject.getString("senderUniqueId")),
                jsonObject.getString("message"), PermissionGroup.valueOf(jsonObject.getString("permissionGroup")));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return new JSONObject(
                Map.of("senderUniqueId", _senderUniqueId.toString(), "message", _message, "permissionGroup",
                        _permissionGroup.name())).toString();
    }

}
