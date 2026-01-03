package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandNetwork extends BaseMultiCommand<MiniPluginPortal> {

    public CommandNetwork(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "network", "Manage the network.", Set.of("net"), MiniPluginPortal.PERM.COMMAND_NETWORK,
                Set.of(
                        new CommandNetworkGroup(miniPluginPortal, miniPluginDatabase),
                        new CommandNetworkServer(miniPluginPortal, miniPluginDatabase),
                        new CommandNetworkSpy(miniPluginPortal),
                        new CommandNetworkMotd(miniPluginPortal, miniPluginDatabase)
                ));
    }

}
