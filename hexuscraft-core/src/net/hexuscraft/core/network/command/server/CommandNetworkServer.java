package net.hexuscraft.core.network.command.server;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.MiniPluginNetwork;

import java.util.Set;

public final class CommandNetworkServer extends BaseMultiCommand<MiniPluginNetwork> {

    public CommandNetworkServer(final MiniPluginNetwork miniPluginNetwork, @SuppressWarnings("unused") final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNetwork, "server", "Manage servers.", Set.of("s"), MiniPluginNetwork.PERM.COMMAND_NETSTAT_SERVER, Set.of());
    }

}
