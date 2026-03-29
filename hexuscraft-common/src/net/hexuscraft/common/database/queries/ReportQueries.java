package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;

import java.util.UUID;

public final class ReportQueries
{

    public static String REPORT(final UUID reportUUID)
    {
        return Database.buildQuery("report", reportUUID.toString());
    }

    public static String LIST_SUBMITTED(final UUID playerUUID)
    {
        return Database.buildQuery("reports", playerUUID.toString(), "submitted");
    }

    public static String LIST_RECEIVED(final UUID playerUUID)
    {
        return Database.buildQuery("reports", playerUUID.toString(), "received");
    }

}
