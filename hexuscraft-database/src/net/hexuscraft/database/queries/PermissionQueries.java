package net.hexuscraft.database.queries;

import net.hexuscraft.database.Database;

public class PermissionQueries {

    public static String PRIMARY(String uuid) {
        return Database.buildQuery("user", uuid, "permission", "primary");
    }

    public static String GROUPS(String uuid) {
        return Database.buildQuery("user", uuid, "permission", "groups");
    }

}
