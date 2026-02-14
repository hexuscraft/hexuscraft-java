package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;

import java.util.Set;

public final class CommandLocate extends BaseMultiCommand<MiniPluginPortal> {

    public CommandLocate(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal,
                "list",
                "Find players across the network.",
                Set.of("glist",
                        "locate",
                        "find",
                        "where"),
                MiniPluginPortal.PERM.COMMAND_LOCATE,
                Set.of(
                        new CommandLocatePlayer(miniPluginPortal),
                        new CommandLocateServer(miniPluginPortal)
                ));
    }

}
