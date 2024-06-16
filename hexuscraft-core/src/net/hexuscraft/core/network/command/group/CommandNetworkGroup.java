package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.network.PluginNetwork;

import java.util.Set;

public final class CommandNetworkGroup extends BaseMultiCommand<HexusPlugin> {

    public CommandNetworkGroup(final PluginNetwork pluginNetwork, final PluginDatabase pluginDatabase) {
        super(pluginNetwork, "group", "Manage server groups.", Set.of("g"), PluginNetwork.PERM.COMMAND_NETSTAT_GROUP, Set.of(
                new CommandNetworkGroupCreate(pluginNetwork, pluginDatabase),
                new CommandNetworkGroupDelete(pluginNetwork, pluginDatabase),
                new CommandNetworkGroupList(pluginNetwork, pluginDatabase)
        ));
    }

}
