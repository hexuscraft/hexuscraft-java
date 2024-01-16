package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
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

    public CommandRankClear(final PluginPermission permission, final PluginDatabase database) {
        super(permission, "clear", "<Player>", "Clears a player's additional groups.", Set.of(), PluginPermission.PERM.COMMAND_RANK_CLEAR);
        _permission = permission;
        _database = database;
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        _database.getJedisPooled().del(PermissionQueries.GROUPS(profile.uuid.toString()));
        sender.sendMessage(F.fMain(this) + "Cleared sub-groups of " + F.fItem(profile.name) + ".");

        final Player player = _miniPlugin._plugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "Your sub-groups were cleared.");
        _permission.refreshPermissions(player);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._plugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
