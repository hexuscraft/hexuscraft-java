package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.MiniPluginHost;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class CommandSetHost extends BaseCommand<MiniPluginHost> {
    public CommandSetHost(final MiniPluginHost miniPluginHost) {
        super(miniPluginHost, "sethost", "[Player]", "Set the host of this server.", Set.of(),
                MiniPluginHost.PERM.COMMAND_SET_HOST);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (args.length == 0) {
            _miniPlugin._hostUniqueId.set(UtilUniqueId.EMPTY_UUID);
            sender.sendMessage(F.fMain(this, "There is no longer a server host."));
            return;
        }

        final Player[] matches =
                PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0],
                        sender, players -> players.length != 1);
        if (matches.length != 1) return;

        final Player newHost = matches[0];
        _miniPlugin._hostUniqueId.set(newHost.getUniqueId());
        sender.sendMessage(F.fMain(this, "Set the server host to ", F.fItem(newHost.getDisplayName()), "."));
        newHost.sendMessage(F.fMain(this, "You are now the host of this server."));
        newHost.playSound(newHost.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1)
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender,
                    false);
        return List.of();
    }
}
