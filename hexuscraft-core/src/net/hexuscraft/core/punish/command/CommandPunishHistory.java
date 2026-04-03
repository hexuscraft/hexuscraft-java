package net.hexuscraft.core.punish.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.CorePunish;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandPunishHistory extends BaseCommand<CorePunish>
{

    public CommandPunishHistory(CorePunish corePunish)
    {
        super(corePunish,
                "punishmenthistory",
                "[Player]",
                "View the history of punishments.",
                Set.of("punishhistory", "xh"),
                CorePunish.PERM.COMMAND_PUNISH_HISTORY);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (!(sender instanceof Player senderPlayer))
        {
            sender.sendMessage(F.fMain(this, F.fError("Only players can view punishment history.")));
            return;
        }

        if (args.length > 1)
        {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() ->
        {
            OfflinePlayer targetOfflinePlayer;

            if (args.length == 1)
            {
                targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(args[0], sender);
                if (targetOfflinePlayer == null)
                {
                    return;
                }
            }
            else
            {
                targetOfflinePlayer = senderPlayer;
            }

            _miniPlugin.openHistoryGui(senderPlayer, targetOfflinePlayer);
        });
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
