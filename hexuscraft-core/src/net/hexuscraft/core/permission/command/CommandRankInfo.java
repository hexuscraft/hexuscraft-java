package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.Set;

public final class CommandRankInfo extends BaseCommand<MiniPluginPermission> {

    final MiniPluginDatabase _miniPluginDatabase;

    CommandRankInfo(final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPermission, "info", "<Player>", "List the groups of a player.", Set.of("i"),
                MiniPluginPermission.PERM.COMMAND_RANK_INFO);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0], sender);
            if (offlinePlayer == null) {
                sender.sendMessage(F.fMatches(new String[]{}, args[0]));
                return;
            }

            final Set<String> groupNames;
            try {
                groupNames = _miniPluginDatabase.getUnifiedJedis()
                        .smembers(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()));
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("An exception occurred while fetching the permission groups of ",
                                F.fItem(offlinePlayer.getName()), ". Please try again later.")));
                return;
            }


            sender.sendMessage(F.fMain(this, F.fItem(offlinePlayer.getName()), " Permission Groups: ",
                    F.fItem(groupNames.stream().map(s -> F.fPermissionGroup(PermissionGroup.valueOf(s))).distinct()
                            .toArray(String[]::new))));
        });

    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1)
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender,
                    false);
        return List.of();
    }

}
