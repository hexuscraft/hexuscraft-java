package net.hexuscraft.web.sales.command;

import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.web.sales.WebSales;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandSalesRankRevoke extends BaseCommand<WebSales>
{

    CoreDatabase _coreDatabase;

    public CommandSalesRankRevoke(WebSales miniPlugin, CoreDatabase coreDatabase)
    {
        super(miniPlugin,
                "revoke",
                "<Username> <Rank>",
                "Revoke a store rank",
                Set.of("remove"),
                WebSales.PERM.COMMAND_SALES_RANK_REVOKE);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        OfflinePlayer target = PlayerSearch.offlinePlayerSearch(args[0], sender);
        if (target == null)
        {
            return;
        }

        PermissionGroup permissionGroup;
        try
        {
            permissionGroup = PermissionGroup.valueOf(args[1]);
        }
        catch (IllegalArgumentException ex)
        {
            sender.sendMessage(F.fMain(this, F.fMatches(new String[0], args[1])));
            return;
        }

        if (!Arrays.asList(WebSales.STORE_RANKS).contains(permissionGroup))
        {
            sender.sendMessage(F.fMain(this,
                    F.fError("Permission group ", F.fItem(permissionGroup.name()), " is not a purchasable rank.")));
            return;
        }

        try
        {
            _coreDatabase._database._jedis.srem(PermissionQueries.GROUPS(target.getUniqueId()), permissionGroup.name());
        }
        catch (JedisException ex)
        {
            sender.sendMessage(F.fMain(this,
                    F.fError("An error occurred while adding permission group. Please try again later or contact an " +
                            "administrator if this issue persists.")));
            _miniPlugin.logSevere(ex);
            return;
        }

        sender.sendMessage(F.fMain(this,
                F.fSuccess("Successfully removed permission group ",
                        F.fItem(permissionGroup.name()),
                        " to ",
                        F.fItem(target.getName()),
                        ".")));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        switch (args.length)
        {
            case 1 ->
            {
                return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                        sender,
                        false);
            }
            case 2 ->
            {
                return List.of(Arrays.stream(WebSales.STORE_RANKS).map(PermissionGroup::name).toArray(String[]::new));
            }
            default ->
            {
                return List.of();
            }
        }
    }
}
