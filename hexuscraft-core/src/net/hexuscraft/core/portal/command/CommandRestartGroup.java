package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandRestartGroup extends BaseCommand<HexusPlugin> {

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

        if (!_portal.doesServerGroupExist(args[0])) {
            sender.sendMessage(F.fMain(this) + F.fError("There is no server group with name " + F.fItem(args[0]) + "."));
            return;
        }

        sender.sendMessage(F.fMain(this, "Sending restart command to servers of group ", F.fItem(args[0]), "."));
        _portal.restartGroup(args[0]);
    }

}
