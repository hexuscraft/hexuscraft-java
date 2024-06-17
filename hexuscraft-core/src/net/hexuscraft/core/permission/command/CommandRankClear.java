package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.database.queries.PermissionQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandRankClear extends BaseCommand<MiniPluginPermission> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandRankClear(final MiniPluginPermission permission, final MiniPluginDatabase database) {
        super(permission, "clear", "<Player>", "Clears a player's additional groups.", Set.of(), MiniPluginPermission.PERM.COMMAND_RANK_CLEAR);
        _miniPluginDatabase = database;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
        if (profile == null) return;

        _miniPluginDatabase.getJedisPooled().del(PermissionQueries.GROUPS(profile.uuid.toString()));
        sender.sendMessage(F.fMain(this) + "Cleared sub-groups of " + F.fItem(profile.name) + ".");

        final Player player = _miniPlugin._hexusPlugin.getServer().getPlayer(profile.name);
        if (player == null) {
            return;
        }

        player.sendMessage(F.fMain(this) + "Your sub-groups were cleared.");
        _miniPlugin.refreshPermissions(player);
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
