package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandMotd extends BaseMultiCommand<MiniPluginPortal> {

    public CommandMotd(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "messageoftheday", "Manage the network MOTD.", Set.of("motd"), MiniPluginPortal.PERM.COMMAND_MOTD, Set.of(
                new CommandMotdView(miniPluginPortal, miniPluginDatabase),
                new CommandMotdSet(miniPluginPortal, miniPluginDatabase)
        ));
    }

}
