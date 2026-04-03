package net.hexuscraft.core.report.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.report.CoreReport;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandReport extends BaseCommand<CoreReport>
{

    public CommandReport(CoreReport coreReport)
    {
        super(coreReport,
                "report",
                "<Player> [Message]",
                "Report a player breaking rules with an optional message.",
                Set.of(),
                CoreReport.PERM.COMMAND_REPORT);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player))
        {
            sender.sendMessage(F.fMain(this) + "Only players can use this command.");
            return;
        }

        OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0], player);
        if (offlinePlayer == null)
        {
            sender.sendMessage(F.fMatches(new String[]{}, args[0]));
            return;
        }

        _miniPlugin.openReportGui(player,
                offlinePlayer,
                String.join(" ", Arrays.stream(args).skip(1).toArray(String[]::new)));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        List<String> names = new ArrayList<>();
        if (args.length == 1)
        {

            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers =
                    _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof Player player)
            {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }

            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
