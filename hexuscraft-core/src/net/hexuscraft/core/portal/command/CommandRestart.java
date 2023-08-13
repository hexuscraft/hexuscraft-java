package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.portal.PluginPortal;

import java.util.Set;

public class CommandRestart extends BaseMultiCommand {

    public CommandRestart(PluginPortal pluginPortal) {
        super(pluginPortal, "reboot", "Restart a server or group of servers.", Set.of("rs"), PluginPortal.PERM.COMMAND_RESTART, Set.of(
                new CommandRestartServer(pluginPortal),
                new CommandRestartGroup(pluginPortal)
        ));
    }

}
