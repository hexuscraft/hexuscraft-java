package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandRestart extends BaseMultiCommand<MiniPluginPortal> {

    public CommandRestart(MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "reboot", "Restart a server or group of servers.", Set.of("rb"), MiniPluginPortal.PERM.COMMAND_RESTART, Set.of(
                new CommandRestartServer(miniPluginPortal),
                new CommandRestartGroup(miniPluginPortal)
        ));
    }

}
