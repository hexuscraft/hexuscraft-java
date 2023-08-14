package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
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

        PluginPortal pluginPortal = (PluginPortal) _miniPlugin;

        sender.sendMessage(F.fMain(this) + "Sending " + F.fItem(args[0]) + " to server " + F.fItem(args[1]) + ".");
        pluginPortal.teleport(args[0], args[1], sender.getName());
    }

}
