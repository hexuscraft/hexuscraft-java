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
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandRankAdd extends BaseCommand {

    final PluginPermission _pluginPermission;
    final PluginDatabase _pluginDatabase;
    final PluginScoreboard _pluginScoreboard;

    CommandRankAdd(PluginPermission pluginPermission, PluginDatabase pluginDatabase, PluginScoreboard pluginScoreboard) {
        super(pluginPermission, "add", "<Player> <Permission Group>", "Add a group to a player.", Set.of("a"), PluginPermission.PERM.COMMAND_RANK_ADD);

        _pluginPermission = pluginPermission;
        _pluginDatabase = pluginDatabase;
        _pluginScoreboard = pluginScoreboard;
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

        _pluginDatabase.getJedisPooled().sadd(PermissionQueries.GROUPS(profile.uuid.toString()), targetGroup.toString());
        sender.sendMessage(F.fMain(this) + "Added sub-group " + F.fPermissionGroup(targetGroup) + " to " + F.fItem(profile.name) + ".");

        Player player = _miniPlugin._javaPlugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "You now have sub-group " + F.fPermissionGroup(targetGroup) + ".");
        _pluginPermission.refreshPermissions(player);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream();
                if (sender instanceof Player player) {
                    streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
                }

                names.addAll(List.of("*", "**"));
                names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
            }
            case 2 -> names.addAll(Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).filter(s -> !s.startsWith("_")).toList());
        }
        return names;
    }

}
