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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandRankRemove extends BaseCommand<CorePermission> {

    private final CoreDatabase _coreDatabase;

    CommandRankRemove(final CorePermission corePermission, final CoreDatabase coreDatabase) {
        super(corePermission,
                "remove",
                "<Player> <Permission Group>",
                "Take a group from a player.",
                Set.of("r"),
                CorePermission.PERM.COMMAND_RANK_REMOVE);
        _coreDatabase = coreDatabase;
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
        } catch (final IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this,
                    F.fError("Invalid group. Groups: ",
                            F.fItem(PermissionGroup.getColoredNames()),
                            ".")));
            return;
        }

        if (targetGroup.toString()
                .startsWith("_")) {
            sender.sendMessage(F.fMain(this,
                    F.fError("This group cannot be manually granted to players.")));
            return;
        }

        if (!sender.hasPermission(targetGroup.name())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0],
                    sender);
            if (offlinePlayer == null) {
                sender.sendMessage(F.fMatches(new String[]{},
                        args[0]));
                return;
            }

            _coreDatabase._database._jedis
                    .srem(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()),
                            targetGroup.name());
            sender.sendMessage(F.fMain(this,
                    "Removed sub-group ",
                    F.fPermissionGroup(targetGroup),
                    " from ",
                    F.fItem(offlinePlayer.getName()),
                    "."));
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers =
                        _miniPlugin._hexusPlugin.getServer()
                                .getOnlinePlayers()
                                .stream();
                if (sender instanceof final Player player) {
                    streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
                }
                names.addAll(streamedOnlinePlayers.map(Player::getName)
                        .toList());
            }
            case 2 -> names.addAll(
                    Arrays.stream(PermissionGroup.values())
                            .map(PermissionGroup::name)
                            .filter(s -> !s.startsWith("_"))
                            .toList());
        }
        return names;
    }

}
