package net.hexuscraft.core.report.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.report.PluginReport;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandReport extends BaseCommand {

    public CommandReport(PluginReport pluginReport) {
        super(pluginReport, "report", "<Player> <Reason>", "Report a player breaking rules.", Set.of(), PluginReport.PERM.COMMAND_REPORT);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        OfflinePlayer target = PlayerSearch.offlinePlayerSearch(_miniPlugin, args[0], sender);
        if (target == null) { return; }

        List<String> reasonArgs = new java.util.ArrayList<>(Arrays.stream(args).toList());
        reasonArgs.remove(0);
        String reason = String.join(" ", reasonArgs);

        _miniPlugin._javaPlugin.getServer().getOnlinePlayers().forEach(player -> {
            if (!player.hasPermission(PermissionGroup.TRAINEE.name())) { return; }
            player.sendMessage(F.fMain(this) + "Report from " + F.fEntity(sender) + ":");
            player.sendMessage(F.fMain("") + "Target: " + F.fEntity(target));
            player.sendMessage(F.fMain("") + "Reason: " + F.fElem(reason));
        });

        sender.sendMessage(F.fMain(this) + "Report against " + F.fEntity(target) + " has been submitted for review. You will receive a response shortly.");
    }
}
