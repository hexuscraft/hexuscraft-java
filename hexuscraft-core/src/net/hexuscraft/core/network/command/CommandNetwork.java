package net.hexuscraft.core.network.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.MiniPluginNetwork;
import net.hexuscraft.core.network.command.group.CommandNetworkGroup;
import net.hexuscraft.core.network.command.server.CommandNetworkServer;

import java.util.Set;

public final class CommandNetwork extends BaseMultiCommand<MiniPluginNetwork> {

    public CommandNetwork(final MiniPluginNetwork miniPluginNetwork, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNetwork, "network", "Manage the network.", Set.of("net"), MiniPluginNetwork.PERM.COMMAND_NETSTAT, Set.of(
                new CommandNetworkGroup(miniPluginNetwork, miniPluginDatabase),
                new CommandNetworkServer(miniPluginNetwork, miniPluginDatabase)
        ));
    }

}
