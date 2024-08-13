package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandNetworkRestart extends BaseMultiCommand<MiniPluginPortal> {

    public CommandNetworkRestart(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "reboot", "Restart a server or group of servers.", Set.of("rb"), MiniPluginPortal.PERM.COMMAND_NETWORK_RESTART, Set.of(
                new CommandNetworkRestartServer(miniPluginPortal),
                new CommandNetworkRestartGroup(miniPluginPortal)
        ));
    }

}
