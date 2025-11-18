package net.hexuscraft.database.queries;

import net.hexuscraft.database.Database;

import java.util.UUID;

public final class PunishQueries {

    public static String LIST(final UUID uniqueId) {
        return Database.buildQuery("user", uniqueId.toString(), "punishments");
    }

    public static String PUNISHMENT(final UUID uniqueId) {
        return Database.buildQuery("punishment", uniqueId.toString());
    }

}
