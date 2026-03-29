package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandNetworkGroupList extends BaseCommand<CorePortal>
{

    CommandNetworkGroupList(CorePortal corePortal)
    {
        super(corePortal,
                "list",
                "",
                "List all server groups.",
                Set.of("l"),
                CorePortal.PERM.COMMAND_NETWORK_GROUP_LIST);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this, "Server Groups: ", F.fItem(_miniPlugin.getServerGroupNames())));
    }

}
