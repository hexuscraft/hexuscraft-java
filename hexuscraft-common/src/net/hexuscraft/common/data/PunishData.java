package net.hexuscraft.common.data;

import net.hexuscraft.common.enums.PunishType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PunishData {

    public final UUID uniqueId; // part of the key name
    public final PunishType type;
    public final Boolean active;
    public final Long origin;
    public final Long length;
    public final String reason;
    public final String server;
    public final UUID staffUniqueId;
    public final String staffServer;

    // these cannot be guaranteed to exist unless 'active' is false. ye be warned!
    public final Long removeOrigin;
    public final String removeReason;
    public final String removeServer;
    public final UUID removeStaffId;
    public final String removeStaffServer;

    public PunishData(final Map<String, String> rawData) {
        uniqueId = UUID.fromString(rawData.get("id"));
        type = PunishType.valueOf(rawData.get("type"));
        active = rawData.get("active").equals("true");
        origin = Long.parseLong(rawData.get("origin"));
        length = Long.parseLong(rawData.get("length"));
        reason = rawData.get("reason");
        server = rawData.get("server");
        staffUniqueId = UUID.fromString(rawData.get("staffId"));
        staffServer = rawData.get("staffServer");

        if (!active) { // we cannot guarantee these should exist unless 'active' is false
            removeOrigin = Long.parseLong(rawData.get("removeOrigin"));
            removeReason = rawData.get("removeReason");
            removeServer = rawData.get("removeServer");
            removeStaffId = UUID.fromString(rawData.get("removeStaffId"));
            removeStaffServer = rawData.get("removeStaffServer");
            return;
        }

        removeOrigin = null;
        removeReason = null;
        removeServer = null;
        removeStaffId = null;
        removeStaffServer = null;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("type", type.name());
        map.put("active", active ? "true" : "false");
        map.put("origin", origin.toString());
        map.put("length", length.toString());
        map.put("reason", reason);
        map.put("server", server);
        map.put("staffId", staffUniqueId.toString());
        map.put("staffServer", staffServer);

        if (!active) {
            map.put("removeOrigin", removeOrigin.toString());
            map.put("removeReason", removeReason);
            map.put("removeServer", removeServer);
            map.put("removeStaffId", removeStaffId.toString());
            map.put("removeStaffServer", removeStaffServer);
        }

        return map;
    }

    @Override
    public String toString() {
        return "PunishData " + uniqueId.toString() + ":" + "\n- type: " + type.toString() + "\n- active: " + active +
                "\n- origin: " + origin + "\n- length: " + length + "\n- reason: " + reason + "\n- staffId: " +
                staffUniqueId.toString() + "\n- server: " + server + "\n- removeOrigin: " + removeOrigin +
                "\n- removeReason: " + removeReason + "\n- removeStaffId: " + removeStaffId.toString() +
                "\n- removeServer: " + removeServer;
    }

    public Long getRemaining(final Long now) {
        if (length == -1) return -1L;

        return length - (now - origin);
    }

    public Long getRemaining() {
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