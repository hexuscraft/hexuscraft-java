package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public final class CommandNetworkServer extends BaseMultiCommand<CorePortal>
{

    public CommandNetworkServer(final CorePortal corePortal, final CoreDatabase coreDatabase)
    {
        super(corePortal,
              "server",
              "Manage servers.",
              Set.of("s"),
              CorePortal.PERM.COMMAND_NETWORK_SERVER,
              Set.of(new CommandNetworkServerRestart(corePortal), new CommandNetworkServerList(corePortal)));
    }

}
