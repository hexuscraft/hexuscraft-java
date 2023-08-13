package net.hexuscraft.database.queries;

import net.hexuscraft.database.Database;

public class PunishQueries {

    public static String LIST(String uuid) {
        return Database.buildQuery("user", uuid, "punishments");
    }

    public static String PUNISHMENT(String uuid) {
        return Database.buildQuery("punishment", uuid);
    }

}
