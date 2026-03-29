package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandSend extends BaseCommand<CorePortal>
{

    public CommandSend(final CorePortal corePortal)
    {
        super(corePortal,
              "send",
              "<Player> <Name>",
              "Teleport a player to a server.",
              Set.of(),
              CorePortal.PERM.COMMAND_SEND);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {
        if (args.length != 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        final String targetName = args[0];
        final String serverName = args[1];

        final OfflinePlayer targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(targetName, sender);
        if (targetOfflinePlayer == null)
        {
            return;
        }

        if (_miniPlugin.getServer(serverName) == null)
        {
            sender.sendMessage(F.fMain(this, F.fError("Could not locate server with name ", F.fItem(serverName), ".")));
            return;
        }

        if (sender instanceof final Player player)
        {
            _miniPlugin._hexusPlugin.runAsync(() -> _miniPlugin.teleportAsync(targetOfflinePlayer.getUniqueId(),
                                                                              serverName,
                                                                              player.getUniqueId()));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> _miniPlugin.teleportAsync(targetOfflinePlayer.getUniqueId(),
                                                                          serverName));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args)
    {
        if (args.length == 1)
        {
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                                                        sender,
                                                        false);
        }
        if (args.length == 2)
        {
            return Arrays.asList(_miniPlugin.getServerNames());
        }
        return List.of();
    }

}
