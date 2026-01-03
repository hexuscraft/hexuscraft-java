package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandNetworkServer extends BaseMultiCommand<MiniPluginPortal> {

    public CommandNetworkServer(final MiniPluginPortal miniPluginPortal,
                                @SuppressWarnings("unused") final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "server", "Manage servers.", Set.of("s"), MiniPluginPortal.PERM.COMMAND_NETWORK_SERVER,
                Set.of(new CommandNetworkServerRestart(miniPluginPortal),
                        new CommandNetworkServerList(miniPluginPortal, miniPluginDatabase)));
    }

}
