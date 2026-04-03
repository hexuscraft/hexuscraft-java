package net.hexuscraft.common.database.messages;

import org.json.JSONObject;
import redis.clients.jedis.UnifiedJedis;

import java.util.Map;

public record PortalRestartServerGroupMessage(String _groupName)
{

    public static String CHANNEL_NAME = "portal.restart.group";

    public static PortalRestartServerGroupMessage fromString(String jsonString)
    {
        return new PortalRestartServerGroupMessage(new JSONObject(jsonString).getString("groupName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.of("groupName", _groupName)).toString();
    }

    public void send(UnifiedJedis jedis)
    {
        jedis.publish(CHANNEL_NAME, toString());
    }

}
