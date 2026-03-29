package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CommandHostSet extends BaseCommand<ArcadeHost>
{
    public CommandHostSet(ArcadeHost arcadeHost)
    {
        super(arcadeHost,
              "set",
              "[Player]",
              "View or set the host of this server.",
              Set.of("s"),
              ArcadeHost.PERM.COMMAND_HOST_SET);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 1)
        {
            sender.sendMessage(help(alias));
            return;
        }

        if (args.length == 0 && _miniPlugin._host.get() == null)
        {
            sender.sendMessage(F.fMain(this, F.fError("There is already no server host.")));
            return;
        }

        Consumer<Player> sendHostRemovalMessage = (Player player) ->
        {
            //noinspection deprecation
            player.sendTitle(C.cYellow + "Server Host", "You are no longer the host of this server.");
            player.sendMessage(F.fMain(this, F.fError("You are no longer the host of this server.")));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
        };

        if (args.length == 0)
        {
            OfflinePlayer oldHost = _miniPlugin._host.getAndSet(null);
            sender.sendMessage(F.fMain(this, "There is no longer a server host."));
            if (oldHost.isOnline())
            {
                sendHostRemovalMessage.accept(oldHost.getPlayer());
            }
            return;
        }

        Player[] matches = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                                                           args[0],
                                                           sender,
                                                           players -> players.length != 1);
        if (matches.length != 1)
        {
            return;
        }

        Player newHost = matches[0];
        OfflinePlayer oldHost = _miniPlugin._host.getAndSet(newHost);

        if (newHost == oldHost)
        {
            sender.sendMessage(F.fMain(this,
                                       F.fError(F.fItem(newHost.getDisplayName()),
                                                " is already the host of this server.")));
            return;
        }

        _miniPlugin._host.set(newHost);
        sender.sendMessage(F.fMain(this, "Set the server host to ", F.fItem(newHost.getDisplayName()), "."));
        //noinspection deprecation
        newHost.sendTitle(C.cYellow + "Server Host", "You are now the host of this server.");
        newHost.sendMessage(F.fMain(this, F.fSuccess("You are now the host of this server.")));
        newHost.playSound(newHost.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
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
        return List.of();
    }
}
