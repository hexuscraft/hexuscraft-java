package net.hexuscraft.web.sales.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.web.sales.WebSales;

import java.util.Set;

public class CommandSales extends BaseMultiCommand<WebSales>
{
    public CommandSales(WebSales miniPlugin, CoreDatabase coreDatabase)
    {
        super(miniPlugin,
                "sales",
                "Store management",
                Set.of("sale"),
                WebSales.PERM.COMMAND_SALES,
                Set.of(new CommandSalesRank(miniPlugin, coreDatabase)));
    }
}
