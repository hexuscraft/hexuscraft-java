package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.messages.ReportSubmittedMessage;
import net.hexuscraft.common.database.queries.ReportQueries;
import net.hexuscraft.common.enums.ReportCloseReason;
import net.hexuscraft.common.enums.ReportSubmitReason;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportData
{

    public UUID reportUUID; // part of the key name
    public UUID senderUUID;
    public UUID targetUUID;
    public String message;
    public ReportSubmitReason reason;
    public Boolean active;
    public Long origin;
    public String server;

    // these cannot be guaranteed to exist unless 'active' is false. ye be warned!
    public Long removeOrigin;
    public ReportCloseReason removeReason;
    public String removeServer;
    public UUID removeStaffUUID;

    public ReportData(Map<String, String> rawData)
    {
        reportUUID = UUID.fromString(rawData.get("uuid"));
        senderUUID = UUID.fromString(rawData.get("senderUUID"));
        targetUUID = UUID.fromString(rawData.get("targetUUID"));
        message = rawData.get("message");
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
        map.put("message", message);
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

    public void submit(UnifiedJedis jedis)
    {
        jedis.hset(ReportQueries.REPORT(reportUUID), toMap());
        jedis.sadd(ReportQueries.LIST_SUBMITTED(senderUUID), reportUUID.toString());
        jedis.sadd(ReportQueries.LIST_RECEIVED(targetUUID), reportUUID.toString());
        jedis.publish(ReportSubmittedMessage.CHANNEL_NAME, new ReportSubmittedMessage(reportUUID).toString());
    }

}