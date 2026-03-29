package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;

import java.util.UUID;

public class PunishQueries
{

    public static String RECEIVED(UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "received");
    }

    public static String ISSUED(UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "issued");
    }

    public static String REVOKED(UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "revoked");
    }

    public static String PUNISHMENT(UUID uuid)
    {
        return Database.buildQuery("punishment", uuid.toString());
    }

}
