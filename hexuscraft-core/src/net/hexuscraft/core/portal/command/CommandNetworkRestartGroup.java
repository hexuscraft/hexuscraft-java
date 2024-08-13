package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkRestartGroup extends BaseCommand<MiniPluginPortal> {

    public CommandNetworkRestartGroup(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "group", "<Server Group>", "Restart all servers of a group.", Set.of("g"), MiniPluginPortal.PERM.COMMAND_NETWORK_RESTART_GROUP);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!_miniPlugin.doesServerGroupExist(args[0])) {
            sender.sendMessage(F.fMain(this) + F.fError("There is no server group with name " + F.fItem(args[0]) + "."));
            return;
        }

        sender.sendMessage(F.fMain(this, "Sending restart command to servers of group ", F.fItem(args[0]), "."));
        _miniPlugin.restartGroup(args[0]);
    }

}
