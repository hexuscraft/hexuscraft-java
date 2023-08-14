package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.scoreboard.PluginScoreboard;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandRankSet extends BaseCommand {

    final PluginDatabase pluginDatabase;
    final PluginScoreboard pluginScoreboard;

    CommandRankSet(PluginPermission pluginPermission, PluginDatabase pluginDatabase, PluginScoreboard pluginScoreboard) {
        super(pluginPermission, "set", "<Player> <Permission Group>", "Set the primary group of a player.", Set.of("s"), PluginPermission.PERM.COMMAND_RANK_SET);
        this.pluginDatabase = pluginDatabase;
        this.pluginScoreboard = pluginScoreboard;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(help(alias));
            return;
        }

        PermissionGroup targetGroup;
        try {
            targetGroup = PermissionGroup.valueOf(args[1]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(F.fMain(this) + F.fError("Invalid group. Groups: ") + F.fList(PermissionGroup.getColoredNames()) + ".");
            return;
        }

        if (targetGroup.toString().startsWith("_")) {
            sender.sendMessage(F.fMain(this) + F.fError("This group cannot be manually granted to players."));
            return;
        }

        if (!sender.hasPermission(targetGroup.toString())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) { return; }

        pluginDatabase.getJedisPooled().set(PermissionQueries.PRIMARY(profile.uuid.toString()), targetGroup.toString());
        sender.sendMessage(F.fMain(this) + "Assigned primary group " + F.fPermissionGroup(targetGroup) + " to " + F.fItem(profile.name) + ".");

        Player player = _miniPlugin._javaPlugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "Your primary group is now " + F.fPermissionGroup(targetGroup) + ".");
        ((PluginPermission) _miniPlugin).refreshPermissions(player);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).toList();
        }
        if (args.length == 2) {
            return Arrays.stream(PermissionGroup.values()).map(permissionGroup -> {
                if (permissionGroup.name().startsWith("_")) {
                    return null;
                }
                return permissionGroup.name();
            }).toList();
        }
        return List.of();
    }

}
