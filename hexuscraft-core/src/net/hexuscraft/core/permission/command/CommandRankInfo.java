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
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandRankInfo extends BaseCommand<CorePermission> {

    final CoreDatabase _coreDatabase;

    CommandRankInfo(final CorePermission corePermission, final CoreDatabase coreDatabase) {
        super(corePermission, "info", "<Player>", "List the groups of a player.", Set.of("i"),
                CorePermission.PERM.COMMAND_RANK_INFO);
        _coreDatabase = coreDatabase;
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

            if (offlinePlayer.isOnline()) {
                final Player player = offlinePlayer.getPlayer();
                if (_miniPlugin._permissionProfiles.containsKey(player)) {
                    sender.sendMessage(F.fMain(this, F.fItem(offlinePlayer.getName()), " Permission Groups: ",
                            F.fItem(Arrays.stream(_miniPlugin._permissionProfiles.get(player)
                                            ._groups())
                                    .map(F::fPermissionGroup)
                                    .toArray(String[]::new))));
                    return;
                }
            }

            final Set<String> groupNames;
            try {
                groupNames = _coreDatabase._database._jedis.smembers(
                        PermissionQueries.GROUPS(offlinePlayer.getUniqueId()));
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("An exception occurred while fetching the permission groups of ",
                                F.fItem(offlinePlayer.getName()), ". Please try again later.")));
                return;
            }

            sender.sendMessage(F.fMain(this, F.fItem(offlinePlayer.getName()), " Permission Groups: ",
                    F.fItem(groupNames.stream()
                            .map(s -> F.fPermissionGroup(PermissionGroup.valueOf(s)))
                            .distinct()
                            .toArray(String[]::new))));
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer()
                .getOnlinePlayers(), sender, false);
        return List.of();
    }

}
