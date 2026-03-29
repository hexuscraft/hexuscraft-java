package net.hexuscraft.common.database.data;

import net.hexuscraft.common.database.messages.PunishPunishmentAppliedMessage;
import net.hexuscraft.common.database.queries.PunishQueries;
import net.hexuscraft.common.enums.PunishType;
import redis.clients.jedis.UnifiedJedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishData
{

    public UUID uuid;
    public PunishType type;
    public Boolean active;
    public Long origin;
    public Long length;
    public String reason;
    public UUID targetUUID;
    public String targetServer;
    public UUID staffUUID;
    public String staffServer;

    // these cannot be guaranteed to be set unless 'active' is FALSE. ye be warned!
    public Long removeOrigin;
    public String removeReason;
    public String removeTargetServer;
    public UUID removeStaffUUID;
    public String removeStaffServer;

    public PunishData(UUID uuid,
                      PunishType type,
                      Boolean active,
                      Long origin,
                      Long length,
                      String reason,
                      UUID targetUUID,
                      String targetServer,
                      UUID staffUUID,
                      String staffServer)
    {
        this.uuid = uuid;
        this.type = type;
        this.active = active;
        this.origin = origin;
        this.length = length;
        this.reason = reason;
        this.targetUUID = targetUUID;
        this.targetServer = targetServer;
        this.staffUUID = staffUUID;
        this.staffServer = staffServer;
    }

    public PunishData(UUID uuid,
                      PunishType type,
                      Boolean active,
                      Long origin,
                      Long length,
                      String reason,
                      UUID targetUUID,
                      String targetServer,
                      UUID staffUUID,
                      String staffServer,

                      Long removeOrigin,
                      String removeReason,
                      String removeTargetServer,
                      UUID removeStaffUUID,
                      String removeStaffServer)
    {
        this.uuid = uuid;
        this.type = type;
        this.active = active;
        this.origin = origin;
        this.length = length;
        this.reason = reason;
        this.targetUUID = targetUUID;
        this.targetServer = targetServer;
        this.staffUUID = staffUUID;
        this.staffServer = staffServer;

        this.removeOrigin = removeOrigin;
        this.removeReason = removeReason;
        this.removeTargetServer = removeTargetServer;
        this.removeStaffUUID = removeStaffUUID;
        this.removeStaffServer = removeStaffServer;
    }

    public PunishData(Map<String, String> map)
    {
        this.uuid = UUID.fromString(map.get("uuid"));
        this.type = PunishType.valueOf(map.get("type"));
        this.active = Boolean.parseBoolean(map.get("active"));
        this.origin = Long.parseLong(map.get("origin"));
        this.length = Long.parseLong(map.get("length"));
        this.reason = map.get("reason");
        this.targetUUID = UUID.fromString(map.get("targetUUID"));
        this.targetServer = map.get("targetServer");
        this.staffUUID = UUID.fromString(map.get("staffUUID"));
        this.staffServer = map.get("staffServer");

        if (this.active)
        {
            return;
        }
        this.removeOrigin = Long.parseLong(map.get("removeOrigin"));
        this.removeReason = map.get("removeReason");
        this.removeTargetServer = map.get("removeTargetServer");
        this.removeStaffUUID = UUID.fromString(map.get("removeStaffUUID"));
        this.removeStaffServer = map.get("removeStaffServer");
    }

    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("uuid", uuid.toString());
        map.put("type", type.name());
        map.put("active", active.toString());
        map.put("origin", origin.toString());
        map.put("length", length.toString());
        map.put("reason", reason);
        map.put("targetUUID", targetUUID.toString());
        map.put("targetServer", targetServer);
        map.put("staffUUID", staffUUID.toString());
        map.put("staffServer", staffServer);

        if (!active)
        {
            map.put("removeOrigin", removeOrigin.toString());
            map.put("removeReason", removeReason);
            map.put("removeTargetServer", removeTargetServer);
            map.put("removeStaffUUID", removeStaffUUID.toString());
            map.put("removeStaffServer", removeStaffServer);
        }

        return map;
    }

    public Long getRemaining()
    {
        return getRemaining(System.currentTimeMillis());
    }

    public Long getRemaining(Long now)
    {
        if (length == -1)
        {
            return -1L;
        }

        return length - (now - origin);
    }

    public PunishData compare(PunishData punishData)
    {

        // Permanent comparison
        if (length == -1)
        {
            if (punishData.length == -1)
            {
                return origin > punishData.origin ? this : punishData;
            }
            return this;
        }
        if (punishData.length == -1)
        {
            return punishData;
        }

        // Temporary comparison
        return getRemaining() > punishData.getRemaining() ? this : punishData;
    }

    public void publish(UnifiedJedis jedis)
    {
        jedis.hset(PunishQueries.PUNISHMENT(uuid), toMap());
        jedis.sadd(PunishQueries.RECEIVED(targetUUID), uuid.toString());
        jedis.sadd(PunishQueries.ISSUED(staffUUID), uuid.toString());
        if (!active)
        {
            jedis.sadd(PunishQueries.ISSUED(removeStaffUUID), uuid.toString());
        }
        jedis.publish(PunishPunishmentAppliedMessage.CHANNEL_NAME, new PunishPunishmentAppliedMessage(this).toString());
    }

}