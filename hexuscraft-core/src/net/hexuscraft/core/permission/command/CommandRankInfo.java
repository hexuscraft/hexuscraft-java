package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        //noinspection ReassignedVariable
        String primaryName = _miniPluginDatabase.getJedisPooled().get(PermissionQueries.PRIMARY(profile.uuid.toString()));
        Set<String> groupNames = _miniPluginDatabase.getJedisPooled().smembers(PermissionQueries.GROUPS(profile.uuid.toString()));

        if (primaryName == null) {
            primaryName = PermissionGroup.MEMBER.toString();
        }

        sender.sendMessage(F.fMain(this) + "Displaying group info for " + F.fItem(profile.name) + ":\n"
                + F.fMain("") + "Primary Group: " + F.fPermissionGroup(PermissionGroup.valueOf(primaryName)) + "\n"
                + F.fMain("") + "Sub Groups: " + F.fList(groupNames.stream().map(s -> F.fPermissionGroup(PermissionGroup.valueOf(s))).distinct().toArray(String[]::new)));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof final Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
