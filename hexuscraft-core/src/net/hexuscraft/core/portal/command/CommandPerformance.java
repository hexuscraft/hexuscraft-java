package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandPerformance extends BaseCommand<MiniPluginPortal> {

    public CommandPerformance(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "performance", "", "View the server and your player performance stats.", Set.of("perf", "lag", "tps"), MiniPluginPortal.PERM.COMMAND_PERFORMANCE);
    }

}
