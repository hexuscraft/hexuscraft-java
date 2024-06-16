package net.hexuscraft.core.network.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.network.PluginNetwork;
import net.hexuscraft.core.network.command.group.CommandNetworkGroup;
import net.hexuscraft.core.network.command.server.CommandNetworkServer;

import java.util.Set;

public final class CommandNetwork extends BaseMultiCommand<HexusPlugin> {

    public CommandNetwork(final PluginNetwork pluginNetwork, final PluginDatabase pluginDatabase) {
        super(pluginNetwork, "network", "Manage the network.", Set.of("net"), PluginNetwork.PERM.COMMAND_NETSTAT, Set.of(
                new CommandNetworkGroup(pluginNetwork, pluginDatabase),
                new CommandNetworkServer(pluginNetwork, pluginDatabase)
        ));
    }

}
