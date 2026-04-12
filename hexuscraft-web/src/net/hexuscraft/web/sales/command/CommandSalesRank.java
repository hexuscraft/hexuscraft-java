package net.hexuscraft.web.sales.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.web.sales.WebSales;

import java.util.Set;

public class CommandSalesRank extends BaseMultiCommand<WebSales>
{
    public CommandSalesRank(WebSales miniPlugin, CoreDatabase coreDatabase)
    {
        super(miniPlugin,
                "rank",
                "Store rank management",
                Set.of(),
                WebSales.PERM.COMMAND_SALES_RANK,
                Set.of(new CommandSalesRankIssue(miniPlugin, coreDatabase)));
    }
}
