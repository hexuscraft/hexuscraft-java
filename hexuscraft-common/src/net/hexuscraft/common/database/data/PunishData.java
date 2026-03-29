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

    public UUID _uuid;
    public PunishType _type;
    public Boolean _active;
    public Long _origin;
    public Long _length;
    public String _reason;
    public UUID _targetUUID;
    public String _targetServer;
    public UUID _staffUUID;
    public String _staffServer;

    // these cannot be guaranteed to be set unless 'active' is FALSE. ye be warned!
    public Long _removeOrigin;
    public String _removeReason;
    public String _removeTargetServer;
    public UUID _removeStaffUUID;
    public String _removeStaffServer;

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
        _uuid = uuid;
        _type = type;
        _active = active;
        _origin = origin;
        _length = length;
        _reason = reason;
        _targetUUID = targetUUID;
        _targetServer = targetServer;
        _staffUUID = staffUUID;
        _staffServer = staffServer;
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
        _uuid = uuid;
        _type = type;
        _active = active;
        _origin = origin;
        _length = length;
        _reason = reason;
        _targetUUID = targetUUID;
        _targetServer = targetServer;
        _staffUUID = staffUUID;
        _staffServer = staffServer;

        _removeOrigin = removeOrigin;
        _removeReason = removeReason;
        _removeTargetServer = removeTargetServer;
        _removeStaffUUID = removeStaffUUID;
        _removeStaffServer = removeStaffServer;
    }

    public PunishData(Map<String, String> map)
    {
        _uuid = UUID.fromString(map.get("uuid"));
        _type = PunishType.valueOf(map.get("type"));
        _active = Boolean.parseBoolean(map.get("active"));
        _origin = Long.parseLong(map.get("origin"));
        _length = Long.parseLong(map.get("length"));
        _reason = map.get("reason");
        _targetUUID = UUID.fromString(map.get("targetUUID"));
        _targetServer = map.get("targetServer");
        _staffUUID = UUID.fromString(map.get("staffUUID"));
        _staffServer = map.get("staffServer");

        if (_active)
        {
            return;
        }
        _removeOrigin = Long.parseLong(map.get("removeOrigin"));
        _removeReason = map.get("removeReason");
        _removeTargetServer = map.get("removeTargetServer");
        _removeStaffUUID = UUID.fromString(map.get("removeStaffUUID"));
        _removeStaffServer = map.get("removeStaffServer");
    }

    public Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>();
        map.put("uuid", _uuid.toString());
        map.put("type", _type.name());
        map.put("active", _active.toString());
        map.put("origin", _origin.toString());
        map.put("length", _length.toString());
        map.put("reason", _reason);
        map.put("targetUUID", _targetUUID.toString());
        map.put("targetServer", _targetServer);
        map.put("staffUUID", _staffUUID.toString());
        map.put("staffServer", _staffServer);

        if (!_active)
        {
            map.put("removeOrigin", _removeOrigin.toString());
            map.put("removeReason", _removeReason);
            map.put("removeTargetServer", _removeTargetServer);
            map.put("removeStaffUUID", _removeStaffUUID.toString());
            map.put("removeStaffServer", _removeStaffServer);
        }

        return map;
    }

    public Long getRemaining()
    {
        return getRemaining(System.currentTimeMillis());
    }

    public Long getRemaining(Long now)
    {
        if (_length == -1)
        {
            return -1L;
        }

        return _length - (now - _origin);
    }

    public PunishData compare(PunishData punishData)
    {

        // Permanent comparison
        if (_length == -1)
        {
            if (punishData._length == -1)
            {
                return _origin > punishData._origin ? this : punishData;
            }
            return this;
        }
        if (punishData._length == -1)
        {
            return punishData;
        }

        // Temporary comparison
        return getRemaining() > punishData.getRemaining() ? this : punishData;
    }

    public void publish(UnifiedJedis jedis)
    {
        jedis.hset(PunishQueries.PUNISHMENT(_uuid), toMap());
        jedis.sadd(PunishQueries.RECEIVED(_targetUUID), _uuid.toString());
        jedis.sadd(PunishQueries.ISSUED(_staffUUID), _uuid.toString());
        if (!_active)
        {
            jedis.sadd(PunishQueries.REVOKED(_removeStaffUUID), _uuid.toString());
        }
        jedis.publish(PunishPunishmentAppliedMessage.CHANNEL_NAME, new PunishPunishmentAppliedMessage(this).toString());
    }

}