package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;

import java.util.UUID;

public final class PunishQueries
{

    public static String RECEIVED(final UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "received");
    }

    public static String ISSUED(final UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "issued");
    }

    public static String REVOKED(final UUID uuid)
    {
        return Database.buildQuery("user", uuid.toString(), "punishments", "revoked");
    }

    public static String PUNISHMENT(final UUID uuid)
    {
        return Database.buildQuery("punishment", uuid.toString());
    }

}
