package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandRankAdd extends BaseCommand<MiniPluginPermission> {

    final MiniPluginDatabase _miniPluginDatabase;

    CommandRankAdd(final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPermission, "add", "<Player> <Permission Group>", "Add a group to a player.", Set.of("a"),
                MiniPluginPermission.PERM.COMMAND_RANK_ADD);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final PermissionGroup targetGroup;
        try {
            targetGroup = PermissionGroup.valueOf(args[1]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(
                    F.fMain(this) + F.fError("Invalid group. Groups: ") + F.fItem(PermissionGroup.getColoredNames()) +
                            ".");
            return;
        }

        if (targetGroup.name().startsWith("_")) {
            sender.sendMessage(F.fMain(this) + F.fError("This group cannot be manually granted to players."));
            return;
        }

        if (!sender.hasPermission(targetGroup.name())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final BukkitScheduler scheduler = _miniPlugin._hexusPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(_miniPlugin._hexusPlugin, () -> {
            final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0], sender);
            if (offlinePlayer == null) return;

            sender.sendMessage(F.fMain(this, "Adding sub-group ", F.fPermissionGroup(targetGroup), " to ",
                    F.fItem(offlinePlayer.getName()), "..."));

            _miniPluginDatabase.getUnifiedJedis()
                    .sadd(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()), targetGroup.name());
            sender.sendMessage(F.fMain(this, F.fSuccess("Added sub-group " + F.fPermissionGroup(targetGroup), " to ",
                    F.fItem(offlinePlayer.getName()), ".")));

            final Player player = _miniPlugin._hexusPlugin.getServer().getPlayer(offlinePlayer.getName());
            if (player == null) return;

            player.sendMessage(F.fMain(this) + "You now have sub-group " + F.fPermissionGroup(targetGroup) + ".");
            _miniPlugin.refreshPermissions(player);
        });

    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, String[] args) {
        if (args.length == 1)
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender,
                    false);
        if (args.length == 2)
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name)
                    .filter(permissionGroupName -> !permissionGroupName.startsWith("_")).toList();
        return List.of();
    }

}
