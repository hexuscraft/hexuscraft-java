package net.hexuscraft.core.punish.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.CorePunish;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandPunish extends BaseCommand<CorePunish>
{

    public CommandPunish(CorePunish corePunish)
    {
        super(corePunish,
              "punishment",
              "<Player> <Reason>",
              "Open the punishment panel.",
              Set.of("punish", "x"),
              CorePunish.PERM.COMMAND_PUNISH);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {
        if (args.length < 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof final Player player))
        {
            sender.sendMessage(F.fMain(this) + "Only players can run this command.");
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() ->
                                          {
                                              final OfflinePlayer
                                                      offlinePlayer
                                                      = PlayerSearch.offlinePlayerSearch(args[0], sender);
                                              if (offlinePlayer == null)
                                              {
                                                  sender.sendMessage(F.fMatches(new String[]{}, args[0]));
                                                  return;
                                              }

                                              final String reasonMessage = String.join(" ",
                                                                                       Arrays.stream(args)
                                                                                             .skip(1)
                                                                                             .toArray(String[]::new));
                                              _miniPlugin.openPunishGui(player, offlinePlayer, reasonMessage);
                                          });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args)
    {
        final List<String> names = new ArrayList<>();
        if (args.length == 1)
        {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer()
                                                                                     .getOnlinePlayers()
                                                                                     .stream();
            if (sender instanceof final Player player)
            {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }

            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
