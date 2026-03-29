package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public class CommandNetwork extends BaseMultiCommand<CorePortal>
{

    public CommandNetwork(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal,
                "network",
                "Manage the network.",
                Set.of("net"),
                CorePortal.PERM.COMMAND_NETWORK,
                Set.of(new CommandNetworkGroup(corePortal, coreDatabase),
                        new CommandNetworkServer(corePortal, coreDatabase),
                        new CommandNetworkSpy(corePortal),
                        new CommandNetworkMotd(corePortal, coreDatabase)));
    }

}
