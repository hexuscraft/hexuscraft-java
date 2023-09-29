package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandRestartServer extends BaseCommand {

    private final PluginPortal _portal;

    public CommandRestartServer(PluginPortal pluginPortal) {
        super(pluginPortal, "server", "<Server>", "Restart a specific server.", Set.of("s", "sv"), PluginPortal.PERM.COMMAND_RESTART_SERVER);
        _portal = pluginPortal;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!_portal.doesServerExist(args[0])) {
            sender.sendMessage(F.fMain(this) + F.fError("Could not locate server with name " + F.fItem(args[0]) + "."));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Sending restart command to server " + F.fItem(args[0]) + ".");
        ((PluginPortal) _miniPlugin).restartServer(args[0]);
    }


}
