package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandNetworkGroup extends BaseMultiCommand<MiniPluginPortal> {

    public CommandNetworkGroup(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "group", "Manage server groups.", Set.of("g"),
                MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP, Set.of(
                        new CommandNetworkGroupCreate(miniPluginPortal, miniPluginDatabase),
                        new CommandNetworkGroupDelete(miniPluginPortal, miniPluginDatabase),
                        new CommandNetworkGroupList(miniPluginPortal, miniPluginDatabase),
                        new CommandNetworkGroupRestart(miniPluginPortal)
                ));
    }

}
