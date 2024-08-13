package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkRestartServer extends BaseCommand<MiniPluginPortal> {

    public CommandNetworkRestartServer(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "server", "<Server>", "Restart a specific server.", Set.of("s", "sv"), MiniPluginPortal.PERM.COMMAND_NETWORK_RESTART_SERVER);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!_miniPlugin.doesServerExistWithName(args[0])) {
            sender.sendMessage(F.fMain(this) + F.fError("Could not locate server with name " + F.fItem(args[0]) + "."));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Sending restart command to server " + F.fItem(args[0]) + ".");
        _miniPlugin.restartServer(args[0]);
    }


}
