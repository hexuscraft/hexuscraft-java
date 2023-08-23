package net.hexuscraft.database.queries;

import net.hexuscraft.database.Database;

import java.util.UUID;

public class ServerQueries {

    public static String SERVER(UUID uuid) {
        return Database.buildQuery("server", uuid.toString());
    }

    public static String SERVERS_ACTIVE() {
        return Database.buildQuery("servers", "active");
    }

    public static String SERVERS_HISTORY() {
        return Database.buildQuery("servers", "history");
    }

    public static String SERVERS_QUEUE() {
        return Database.buildQuery("servers", "queue");
    }

    public static String SERVERGROUP(UUID uuid) {
        return Database.buildQuery("servergroup", uuid.toString());
    }

    public static String SERVERGROUPS_ACTIVE() {
        return Database.buildQuery("servergroups", "active");
    }

    public static String SERVERGROUPS_HISTORY() {
        return Database.buildQuery("servergroups", "history");
    }

}
