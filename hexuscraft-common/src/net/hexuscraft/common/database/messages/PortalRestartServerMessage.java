package net.hexuscraft.common.database.messages;

import org.json.JSONObject;
import redis.clients.jedis.UnifiedJedis;

import java.util.Map;

public record PortalRestartServerMessage(String _serverName)
{

    public static String CHANNEL_NAME = "portal.restart.server";

    public static PortalRestartServerMessage fromString(String jsonString)
    {
        return new PortalRestartServerMessage(new JSONObject(jsonString).getString("serverName"));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString()
    {
        return new JSONObject(Map.of("serverName", _serverName)).toString();
    }

    public void send(UnifiedJedis jedis)
    {
        jedis.publish(CHANNEL_NAME, toString());
    }

}
