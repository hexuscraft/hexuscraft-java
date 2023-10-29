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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandRankClear extends BaseCommand {

    private final PluginPermission _permission;
    private final PluginDatabase _database;
    private final PluginScoreboard _scoreboard;

    public CommandRankClear(final PluginPermission permission, final PluginDatabase database, final PluginScoreboard scoreboard) {
        super(permission, "clear", "<Player>", "Clears a player's additional groups.", Set.of(), PluginPermission.PERM.COMMAND_RANK_CLEAR);
        _permission = permission;
        _database = database;
        _scoreboard = scoreboard;
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

        if (targetGroup.toString().startsWith("_")) {
            sender.sendMessage(F.fMain(this) + F.fError("This group cannot be manually granted to players."));
            return;
        }

        if (!sender.hasPermission(targetGroup.toString())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        _database.getJedisPooled().del(PermissionQueries.GROUPS(profile.uuid.toString()));
        sender.sendMessage(F.fMain(this) + "Cleared sub-groups of " + F.fItem(profile.name) + ".");

        final Player player = _miniPlugin._javaPlugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "You now have sub-group " + F.fPermissionGroup(targetGroup) + ".");
        _permission.refreshPermissions(player);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
