package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;

import java.util.Set;

public class CommandMotd extends BaseMultiCommand<HexusPlugin> {

    public CommandMotd(final PluginPortal pluginPortal, final PluginDatabase pluginDatabase) {
        super(pluginPortal, "messageoftheday", "Manage the network MOTD.", Set.of("motd"), PluginPortal.PERM.COMMAND_MOTD, Set.of(
                new CommandMotdView(pluginPortal, pluginDatabase),
                new CommandMotdSet(pluginPortal, pluginDatabase)
        ));
    }

}
