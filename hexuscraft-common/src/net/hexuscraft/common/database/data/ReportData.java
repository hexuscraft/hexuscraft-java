package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.messages.ReportSubmittedMessage;
import net.hexuscraft.common.database.queries.ReportQueries;
import net.hexuscraft.common.enums.ReportCloseReason;
import net.hexuscraft.common.enums.ReportSubmitReason;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ReportData
{

    public final UUID reportUUID; // part of the key name
    public final UUID senderUUID;
    public final UUID targetUUID;
    public final ReportSubmitReason reason;
    public final Boolean active;
    public final Long origin;
    public final String server;

    // these cannot be guaranteed to exist unless 'active' is false. ye be warned!
    public final Long removeOrigin;
    public final ReportCloseReason removeReason;
    public final String removeServer;
    public final UUID removeStaffUUID;

    public ReportData(final Map<String, String> rawData)
    {
        reportUUID = UUID.fromString(rawData.get("reportUUID"));
        senderUUID = UUID.fromString(rawData.get("senderUUID"));
        targetUUID = UUID.fromString(rawData.get("targetUUID"));
        reason = ReportSubmitReason.valueOf(rawData.get("reason"));
        active = rawData.get("active").equals("true");
        origin = Long.parseLong(rawData.get("origin"));
        server = rawData.get("server");

        if (!active)
        { // we cannot guarantee these should exist unless 'active' is false
            removeOrigin = Long.parseLong(rawData.get("removeOrigin"));
            removeReason = ReportCloseReason.valueOf(rawData.get("removeReason"));
            removeServer = rawData.get("removeServer");
            removeStaffUUID = UUID.fromString(rawData.get("removeStaffId"));
            return;
        }

        removeOrigin = null;
        removeReason = null;
        removeServer = null;
        removeStaffUUID = null;
    }

    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("senderUUID", senderUUID.toString());
        map.put("targetUUID", targetUUID.toString());
        map.put("reason", reason.name());
        map.put("active", Boolean.toString(active));
        map.put("origin", origin.toString());
        map.put("server", server);

        if (!active)
        {
            map.put("removeOrigin", removeOrigin.toString());
            map.put("removeReason", removeReason.name());
            map.put("removeServer", removeServer);
            map.put("removeStaffId", removeStaffUUID.toString());
        }

        return map;
    }

    public void submit(final UnifiedJedis jedis)
    {
        jedis.hset(ReportQueries.REPORT(reportUUID), toMap());
        jedis.sadd(ReportQueries.LIST_SUBMITTED(senderUUID), reportUUID.toString());
        jedis.sadd(ReportQueries.LIST_RECEIVED(targetUUID), reportUUID.toString());
        jedis.publish(ReportSubmittedMessage.CHANNEL_NAME, new ReportSubmittedMessage(reportUUID).toString());
    }

}