package net.hexuscraft.common.database.queries;

import net.hexuscraft.common.database.Database;

import java.util.UUID;

public class ReportQueries
{

    public static String REPORT(UUID reportUUID)
    {
        return Database.buildQuery("report", reportUUID.toString());
    }

    public static String LIST_SUBMITTED(UUID playerUUID)
    {
        return Database.buildQuery("user", playerUUID.toString(), "reports", "submitted");
    }

    public static String LIST_RECEIVED(UUID playerUUID)
    {
        return Database.buildQuery("user", playerUUID.toString(), "reports", "received");
    }

}
