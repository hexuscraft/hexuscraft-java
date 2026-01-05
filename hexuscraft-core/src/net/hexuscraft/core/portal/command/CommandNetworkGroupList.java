package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkGroupList extends BaseCommand<MiniPluginPortal> {

    CommandNetworkGroupList(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "list", "", "List all server groups.", Set.of("l"),
                MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_LIST);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this, "Server Groups: ", F.fItem(_miniPlugin.getServerGroupNames())));
    }

}
