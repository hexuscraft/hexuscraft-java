package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandRestartGroup extends BaseCommand {

    PluginPortal _portal;

    public CommandRestartGroup(PluginPortal pluginPortal) {
        super(pluginPortal, "group", "<Server Group>", "Restart all servers of a group.", Set.of("g"), PluginPortal.PERM.COMMAND_RESTART_GROUP);
        _portal = pluginPortal;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Sending restart command to servers of group " + F.fItem(args[0]) + ".");
        _portal.restartGroup(args[0]);
    }

}
