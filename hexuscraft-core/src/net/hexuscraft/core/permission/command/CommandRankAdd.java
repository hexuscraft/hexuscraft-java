package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public final class CommandRankAdd extends BaseCommand<CorePermission>
{

    final CoreDatabase _coreDatabase;

    CommandRankAdd(final CorePermission corePermission, final CoreDatabase coreDatabase)
    {
        super(corePermission,
              "add",
              "<Player> <Permission Group>",
              "Add a group to a player.",
              Set.of("a"),
              CorePermission.PERM.COMMAND_RANK_ADD);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {
        if (args.length != 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        final PermissionGroup targetGroup;
        try
        {
            targetGroup = Arrays.stream(PermissionGroup.values())
                                .filter(group -> group.name().equalsIgnoreCase(args[1]))
                                .findFirst()
                                .orElseThrow();
        }
        catch (final NoSuchElementException ex)
        {
            sender.sendMessage(F.fMain(this, F.fError("You specified an invalid group name: ", F.fItem(args[1]))) +
                               "\n" +
                               F.fMain("", "Available groups: ") +
                               F.fItem(PermissionGroup.getColoredNames()) +
                               ".");
            return;
        }

        if (targetGroup.name().startsWith("_"))
        {
            sender.sendMessage(F.fMain(this) + F.fError("This group cannot be manually granted to players."));
            return;
        }

        if (!sender.hasPermission(targetGroup.name()))
        {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final BukkitScheduler scheduler = _miniPlugin._hexusPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(_miniPlugin._hexusPlugin, () ->
        {
            final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0], sender);
            if (offlinePlayer == null)
            {
                return;
            }

            sender.sendMessage(F.fMain(this,
                                       "Adding sub-group ",
                                       F.fPermissionGroup(targetGroup),
                                       " to ",
                                       F.fItem(offlinePlayer.getName()),
                                       "..."));

            _coreDatabase._database._jedis.sadd(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()),
                                                targetGroup.name());
            sender.sendMessage(F.fMain(this,
                                       F.fSuccess("Added sub-group " + F.fPermissionGroup(targetGroup),
                                                  " to ",
                                                  F.fItem(offlinePlayer.getName()),
                                                  ".")));
        });

    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, String[] args)
    {
        if (args.length == 1)
        {
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                                                        sender,
                                                        false);
        }
        if (args.length == 2)
        {
            return Arrays.stream(PermissionGroup.values())
                         .map(PermissionGroup::name)
                         .filter(permissionGroupName -> !permissionGroupName.startsWith("_"))
                         .toList();
        }
        return List.of();
    }

}
