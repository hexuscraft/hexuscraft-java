package net.hexuscraft.database.queries;

import net.hexuscraft.database.Database;

import java.util.UUID;

public final class PermissionQueries {

    public static String PRIMARY(final UUID uuid) {
        return Database.buildQuery("user", uuid.toString(), "permission", "primary");
    }

    public static String GROUPS(final UUID uuid) {
        return Database.buildQuery("user", uuid.toString(), "permission", "groups");
    }

}
