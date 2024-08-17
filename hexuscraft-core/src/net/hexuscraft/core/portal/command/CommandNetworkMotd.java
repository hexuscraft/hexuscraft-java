package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandNetworkMotd extends BaseMultiCommand<MiniPluginPortal> {

    public CommandNetworkMotd(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "messageoftheday", "Manage the network MOTD.", Set.of("motd"), MiniPluginPortal.PERM.COMMAND_MOTD, Set.of(
                new CommandNetworkMotdView(miniPluginPortal, miniPluginDatabase),
                new CommandNetworkMotdSet(miniPluginPortal, miniPluginDatabase)
        ));
    }

}
