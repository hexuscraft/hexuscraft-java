package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandRankInfo extends BaseCommand {

    final PluginDatabase _pluginDatabase;

    CommandRankInfo(PluginPermission pluginPermission, PluginDatabase pluginDatabase) {
        super(pluginPermission, "info", "<Player>", "List the groups of a player.", Set.of("i"), PluginPermission.PERM.COMMAND_RANK_INFO);
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) { return; }

        //noinspection ReassignedVariable
        String primaryName = _pluginDatabase.getJedisPooled().get(PermissionQueries.PRIMARY(profile.uuid.toString()));
        Set<String> groupNames = _pluginDatabase.getJedisPooled().smembers(PermissionQueries.GROUPS(profile.uuid.toString()));

        if (primaryName == null) {
            primaryName = PermissionGroup.MEMBER.toString();
        }

        sender.sendMessage(F.fMain(this) + "Displaying group info for " + F.fItem(profile.name) + ":\n"
                + F.fMain() + "Primary Group: " + F.fPermissionGroup(PermissionGroup.valueOf(primaryName)) + "\n"
                + F.fMain() + "Sub Groups: " + F.fList(groupNames.stream().map(s -> F.fPermissionGroup(PermissionGroup.valueOf(s))).distinct().toArray(String[]::new)));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            names.addAll(Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).filter(s -> !s.startsWith("_")).toList());
        }
        return names;
    }

}
