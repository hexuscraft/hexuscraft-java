package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.MiniPluginPunish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class CommandPunishHistory extends BaseCommand<MiniPluginPunish> {

    public CommandPunishHistory(MiniPluginPunish miniPluginPunish) {
        super(miniPluginPunish, "punishmenthistory", "[Player]", "View the history of punishments.", Set.of("punishhistory", "xh"), MiniPluginPunish.PERM.COMMAND_PUNISH_HISTORY);
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final UUID targetUUID;
        final String targetName;

        if (args.length == 1) {
            MojangProfile profile = PlayerSearch.fetchMojangProfile(args[0], sender);
            if (profile == null) return;
            targetUUID = profile.uuid;
            targetName = profile.name;
        } else if (sender instanceof final Player player) {
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            sender.sendMessage(F.fMain(this) + "Only players can view their own punishment history.");
            return;
        }

        sender.sendMessage(F.fMain(this) + "Viewing punishment history of " + F.fItem(targetName) + ".");

    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
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
