package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public class CommandLocate extends BaseMultiCommand<CorePortal>
{

    public CommandLocate(CorePortal corePortal)
    {
        super(corePortal,
              "list",
              "Find players across the network.",
              Set.of("glist", "locate", "find", "where"),
              CorePortal.PERM.COMMAND_LOCATE,
              Set.of(new CommandLocatePlayer(corePortal), new CommandLocateServer(corePortal)));
    }

}
