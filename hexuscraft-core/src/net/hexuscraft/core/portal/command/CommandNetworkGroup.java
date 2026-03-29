package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public class CommandNetworkGroup extends BaseMultiCommand<CorePortal>
{

    public CommandNetworkGroup(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal,
              "group",
              "Manage server groups.",
              Set.of("g"),
              CorePortal.PERM.COMMAND_NETWORK_GROUP,
              Set.of(new CommandNetworkGroupCreate(corePortal, coreDatabase),
                     new CommandNetworkGroupDelete(corePortal, coreDatabase),
                     new CommandNetworkGroupList(corePortal),
                     new CommandNetworkGroupRestart(corePortal)));
    }

}
