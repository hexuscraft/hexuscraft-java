package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.MiniPluginNetwork;

import java.util.Set;

public final class CommandNetworkGroup extends BaseMultiCommand<MiniPluginNetwork> {

    public CommandNetworkGroup(final MiniPluginNetwork miniPluginNetwork, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNetwork, "group", "Manage server groups.", Set.of("g"), MiniPluginNetwork.PERM.COMMAND_NETSTAT_GROUP, Set.of(
                new CommandNetworkGroupCreate(miniPluginNetwork, miniPluginDatabase),
                new CommandNetworkGroupDelete(miniPluginNetwork, miniPluginDatabase),
                new CommandNetworkGroupList(miniPluginNetwork, miniPluginDatabase)
        ));
    }

}
