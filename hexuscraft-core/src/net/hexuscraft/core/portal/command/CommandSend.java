package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
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

public class CommandSend extends BaseCommand<CorePortal>
{

    public CommandSend(CorePortal corePortal)
    {
        super(corePortal,
                "send",
                "<Player> <Name>",
                "Teleport a player to a server.",
                Set.of(),
                CorePortal.PERM.COMMAND_SEND);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length != 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        String targetName = args[0];
        String serverName = args[1];

        OfflinePlayer target = PlayerSearch.offlinePlayerSearch(targetName, sender);
        if (target == null)
        {
            return;
        }

        ServerData server = _miniPlugin.getServer(serverName);
        if (server == null)
        {
            sender.sendMessage(F.fMain(this, F.fError("Could not locate server with name ", F.fItem(serverName), ".")));
            return;
        }

        if (sender instanceof Player player)
        {
            _miniPlugin._hexusPlugin.runAsync(() -> _miniPlugin.teleportAsync(target.getUniqueId(),
                    server._name,
                    player.getUniqueId()));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> _miniPlugin.teleportAsync(target.getUniqueId(), server._name));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
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
