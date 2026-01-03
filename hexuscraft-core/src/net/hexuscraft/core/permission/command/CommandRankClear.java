package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandRankClear extends BaseCommand<MiniPluginPermission> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandRankClear(final MiniPluginPermission permission, final MiniPluginDatabase database) {
        super(permission, "clear", "<Player>", "Clears a player's additional groups.", Set.of(),
                MiniPluginPermission.PERM.COMMAND_RANK_CLEAR);
        _miniPluginDatabase = database;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0]);
            if (offlinePlayer == null) {
                sender.sendMessage(F.fMatches(new String[]{}, args[0]));
                return;
            }

            sender.sendMessage(F.fMain(this, "Clearing sub-groups of ", F.fItem(offlinePlayer.getName()), "..."));

            _miniPluginDatabase.getUnifiedJedis().del(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()));
            sender.sendMessage(
                    F.fMain(this, F.fSuccess("Cleared sub-groups of ", F.fItem(offlinePlayer.getName()), ".")));

            final Player player = _miniPlugin._hexusPlugin.getServer().getPlayer(offlinePlayer.getName());
            if (player == null) {
                return;
            }

            player.sendMessage(F.fMain(this, F.fSuccess("Your sub-groups were cleared.")));
            _miniPlugin.refreshPermissions(player);
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers =
                    _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof final Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
