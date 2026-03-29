package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;

import java.util.Set;

public final class CommandPerformance extends BaseCommand<CorePortal>
{

    public CommandPerformance(final CorePortal corePortal)
    {
        super(corePortal,
              "performance",
              "",
              "View the server and your player performance stats.",
              Set.of("perf", "lag", "tps"),
              CorePortal.PERM.COMMAND_PERFORMANCE);
    }

}
