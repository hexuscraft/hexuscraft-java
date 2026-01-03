package net.hexuscraft.core.report.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.report.MiniPluginReport;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandReport extends BaseCommand<MiniPluginReport> {

    public CommandReport(MiniPluginReport miniPluginReport) {
        super(miniPluginReport, "report", "<Player> <Reason>", "Report a player breaking rules.", Set.of(),
                MiniPluginReport.PERM.COMMAND_REPORT);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this) + "Only players can use this command.");
            return;
        }

        final OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0], player);
        if (offlinePlayer == null) {
            sender.sendMessage(F.fMatches(new String[]{}, args[0]));
            return;
        }

        List<String> reasonArgs = new java.util.ArrayList<>(Arrays.stream(args).toList());
        reasonArgs.removeFirst();
        String reason = String.join(" ", reasonArgs);

        _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
            if (!onlinePlayer.hasPermission(PermissionGroup.TRAINEE.name())) return;
            onlinePlayer.sendMessage(
                    F.fMain(this, F.fItem(offlinePlayer.getName()), " reported by ", F.fItem(player.getDisplayName()),
                            ":\n",
                            F.fMain("", F.fItem(reason))));
        });

        sender.sendMessage(F.fMain(this, "Report against ", F.fItem(offlinePlayer.getName()),
                " has been submitted for review. You will receive a response shortly."));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
        if (args.length == 1) {

            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer()
                    .getOnlinePlayers().stream();
            if (sender instanceof final Player player)
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));

            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
