package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public class CommandNetworkMotd extends BaseMultiCommand<CorePortal>
{

    public CommandNetworkMotd(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal,
              "messageoftheday",
              "Manage the network MOTD.",
              Set.of("motd"),
              CorePortal.PERM.COMMAND_MOTD,
              Set.of(new CommandNetworkMotdView(corePortal, coreDatabase),
                     new CommandNetworkMotdSet(corePortal, coreDatabase)));
    }

}
