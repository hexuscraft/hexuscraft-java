package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
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

public class CommandRankAdd extends BaseCommand<HexusPlugin> {

    final PluginPermission _pluginPermission;
    final PluginDatabase _pluginDatabase;

    CommandRankAdd(final PluginPermission pluginPermission, final PluginDatabase pluginDatabase) {
        super(pluginPermission, "add", "<Player> <Permission Group>", "Add a group to a player.", Set.of("a"), PluginPermission.PERM.COMMAND_RANK_ADD);

        _pluginPermission = pluginPermission;
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final PermissionGroup targetGroup;
        try {
            targetGroup = PermissionGroup.valueOf(args[1]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this) + F.fError("Invalid group. Groups: ") + F.fList(PermissionGroup.getColoredNames()) + ".");
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

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        _pluginDatabase.getJedisPooled().sadd(PermissionQueries.GROUPS(profile.uuid.toString()), targetGroup.name());
        sender.sendMessage(F.fMain(this) + "Added sub-group " + F.fPermissionGroup(targetGroup) + " to " + F.fItem(profile.name) + ".");

        final Player player = _miniPlugin._plugin.getServer().getPlayer(profile.name);
        if (player == null) return;

        player.sendMessage(F.fMain(this) + "You now have sub-group " + F.fPermissionGroup(targetGroup) + ".");
        _pluginPermission.refreshPermissions(player);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, String[] args) {
        final List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._plugin.getServer().getOnlinePlayers().stream();
                if (sender instanceof Player player) {
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
