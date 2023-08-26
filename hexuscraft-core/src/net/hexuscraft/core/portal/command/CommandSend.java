package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandSend extends BaseCommand {

    public CommandSend(PluginPortal pluginPortal) {
        super(pluginPortal, "send", "<Player> <Name>", "Teleport a player to a server.", Set.of(), PluginPortal.PERM.COMMAND_SEND);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(help(alias));
            return;
        }

        String targetName = args[0];
        String serverName = args[1];

        if (PlayerSearch.fetchMojangProfile(targetName, sender) == null) {
            return;
        }

        if (!((PluginPortal) _miniPlugin).isServerActive(serverName)) {
            sender.sendMessage(F.fMain(this) + "Could not locate a server with name " + F.fItem(serverName) + ".");
            return;
        }

        PluginPortal pluginPortal = (PluginPortal) _miniPlugin;

        sender.sendMessage(F.fMain(this) + "Sending " + F.fItem(targetName) + " to server " + F.fItem(serverName) + ".");
        pluginPortal.teleport(targetName, serverName, sender.getName());
    }

}
