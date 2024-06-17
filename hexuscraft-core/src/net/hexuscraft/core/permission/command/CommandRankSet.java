package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandRankSet extends BaseCommand<MiniPluginPermission> {

    final MiniPluginDatabase _miniPluginDatabase;

    CommandRankSet(final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPermission, "set", "<Player> <Permission Group>", "Set the primary group of a player.", Set.of("s"), MiniPluginPermission.PERM.COMMAND_RANK_SET);
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
        } catch (final IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this) + F.fError("Invalid group. Groups: ") + F.fList(PermissionGroup.getColoredNames()) + ".");
            return;
        }

        if (targetGroup.toString().startsWith("_")) {
            sender.sendMessage(F.fMain(this) + F.fError("This group cannot be manually granted to players."));
            return;
        }

        if (!sender.hasPermission(targetGroup.name())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        _miniPluginDatabase.getJedisPooled().set(PermissionQueries.PRIMARY(profile.uuid.toString()), targetGroup.name());
        sender.sendMessage(F.fMain(this) + "Assigned primary group " + F.fPermissionGroup(targetGroup) + " to " + F.fItem(profile.name) + ".");

        final Player player = _miniPlugin._hexusPlugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "Your primary group is now " + F.fPermissionGroup(targetGroup) + ".");
        _miniPlugin.refreshPermissions(player);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
                if (sender instanceof final Player player) {
                    streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
                }
                names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
            }
            case 2 ->
                    names.addAll(Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).filter(s -> !s.startsWith("_")).toList());
        }
        return names;
    }

}
