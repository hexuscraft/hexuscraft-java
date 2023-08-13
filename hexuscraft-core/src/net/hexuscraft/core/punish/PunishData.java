package net.hexuscraft.core.punish;

import org.bukkit.Bukkit;

import javax.sound.midi.SysexMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishData {

    public final UUID id; // part of the key name
    public final PunishType type;
    public final Boolean active;
    public final Long origin;
    public final Long length;
    public final String reason;
    public final UUID serverId;
    public final UUID staffId;
    public final UUID staffServerId;

    // these cannot be guarenteed to exist unless 'active' is false. ye be warned!
    public final Long removeOrigin;
    public final String removeReason;
    public final UUID removeServerId;
    public final UUID removeStaffId;
    public final UUID removeStaffServerId;

    public PunishData(UUID id, PunishType type, boolean active, Long origin, Long length, String reason, UUID serverId, UUID staffId, UUID staffServerId, Long removeOrigin, String removeReason, UUID removeServerId, UUID removeStaffId, UUID removeStaffServerId) {
        this.id = id;
        this.type = type;
        this.active = active;
        this.origin = origin;
        this.length = length;
        this.reason = reason;
        this.serverId = serverId;
        this.staffId = staffId;
        this.staffServerId = staffServerId;

        this.removeOrigin = removeOrigin;
        this.removeReason = removeReason;
        this.removeServerId = removeServerId;
        this.removeStaffId = removeStaffId;
        this.removeStaffServerId = removeStaffServerId;
    }

    public PunishData(Map<String, String> rawData) {
        id = UUID.fromString(rawData.get("id"));
        type = PunishType.valueOf(rawData.get("type"));
        active = rawData.get("active").equals("true");
        origin = Long.parseLong(rawData.get("origin"));
        length = Long.parseLong(rawData.get("length"));
        reason = rawData.get("reason");
        serverId = UUID.fromString(rawData.get("serverId"));
        staffId = UUID.fromString(rawData.get("staffId"));
        staffServerId = UUID.fromString(rawData.get("staffServerId"));

        if (!active) { // we cannot guarantee these should exist unless 'active' is false
            removeOrigin = Long.parseLong(rawData.get("removeOrigin"));
            removeReason = rawData.get("removeReason");
            removeServerId = UUID.fromString(rawData.get("removeServerId"));
            removeStaffId = UUID.fromString(rawData.get("removeStaffId"));
            removeStaffServerId = UUID.fromString(rawData.get("removeStaffServerId"));
            return;
        }

        removeOrigin = null;
        removeReason = null;
        removeServerId = null;
        removeStaffId = null;
        removeStaffServerId = null;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("type", type.name());
        map.put("active", active ? "true" : "false");
        map.put("origin", origin.toString());
        map.put("length", length.toString());
        map.put("reason", reason);
        map.put("serverId", serverId.toString());
        map.put("staffId", staffId.toString());
        map.put("staffServerId", staffServerId.toString());

        if (!active) {
            map.put("removeOrigin", removeOrigin.toString());
            map.put("removeReason", removeReason);
            map.put("removeServerId", removeServerId.toString());
            map.put("removeStaffId", removeStaffId.toString());
            map.put("removeStaffServerId", removeStaffServerId.toString());
        }

        return map;
    }

    @Override
    public String toString() {
        return "PunishData " + id.toString() + ":"
                + "\n- type: " + type.toString()
                + "\n- active: " + active
                + "\n- origin: " + origin
                + "\n- length: " + length
                + "\n- reason: " + reason
                + "\n- staffId: " + staffId.toString()
                + "\n- serverId: " + serverId.toString()
                + "\n- removeOrigin: " + removeOrigin
                + "\n- removeReason: " + removeReason
                + "\n- removeStaffId: " + removeStaffId.toString()
                + "\n- removeServerId: " + removeServerId.toString();
    }

    public final Long getRemaining(Long now) {
        return length - (now - origin);
    }

    public final Long getRemaining() {
        return getRemaining(System.currentTimeMillis());
    }

    public PunishData compare(PunishData punishData) {

        // Permanent comparison
        if (length == -1) {
            if (punishData.length == -1) {
                return origin > punishData.origin ? this : punishData;
            }
            return this;
        }
        if (punishData.length == -1) {
            return punishData;
        }

        // Temporary comparison
        Long currentMillis = System.currentTimeMillis();
        return getRemaining(currentMillis) > punishData.getRemaining(currentMillis) ? this : punishData;
    }

}