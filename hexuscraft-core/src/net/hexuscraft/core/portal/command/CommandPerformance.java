package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public class CommandPerformance extends BaseCommand<CorePortal>
{

    public CommandPerformance(CorePortal corePortal)
    {
        super(corePortal,
              "performance",
              "",
              "View the server and your player performance stats.",
              Set.of("perf", "lag", "tps"),
              CorePortal.PERM.COMMAND_PERFORMANCE);
    }

}
