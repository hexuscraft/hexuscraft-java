package net.hexuscraft.core.network.command.server;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.network.PluginNetwork;

import java.util.Set;

public final class CommandNetworkServer extends BaseMultiCommand<HexusPlugin> {

    public CommandNetworkServer(final PluginNetwork pluginNetwork, @SuppressWarnings("unused") final PluginDatabase pluginDatabase) {
        super(pluginNetwork, "server", "Manage servers.", Set.of("s"), PluginNetwork.PERM.COMMAND_NETSTAT_SERVER, Set.of());
    }

}
