package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public final class CommandRankInfo extends BaseCommand<MiniPluginPermission> {

    final MiniPluginDatabase _miniPluginDatabase;

    CommandRankInfo(final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPermission, "info", "<Player>", "List the groups of a player.", Set.of("i"), MiniPluginPermission.PERM.COMMAND_RANK_INFO);
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

            final String fetchedPrimaryName = _miniPluginDatabase.getJedisPooled().get(PermissionQueries.PRIMARY(offlinePlayer.getUniqueId()));
            final String primaryName = fetchedPrimaryName == null ? PermissionGroup.MEMBER.name() : fetchedPrimaryName;

            final Set<String> groupNames = _miniPluginDatabase.getJedisPooled().smembers(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()));

            sender.sendMessage(F.fMain(this, "Displaying group info for ", F.fItem(offlinePlayer.getName()), ":\n", F.fMain("", "Primary Group: ", F.fPermissionGroup(PermissionGroup.valueOf(primaryName)), " (", F.fItem(primaryName), ")"), "\n", F.fMain("", "Sub Groups: ", F.fList(groupNames.stream().map(s -> F.fPermissionGroup(PermissionGroup.valueOf(s))).distinct().toArray(String[]::new)))));
        });

    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1)
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender, false);
        return List.of();
    }

}
