package net.hexuscraft.core.netstat.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.PluginNetStat;
import net.hexuscraft.core.netstat.command.group.CommandNetStatGroup;
import net.hexuscraft.core.netstat.command.server.CommandNetStatServer;

import java.util.Set;

public class CommandNetStat extends BaseMultiCommand<HexusPlugin> {

    public CommandNetStat(final PluginNetStat pluginNetStat, final PluginDatabase pluginDatabase) {
        super(pluginNetStat, "netstat", "Manage the network.", Set.of("net"), PluginNetStat.PERM.COMMAND_NETSTAT, Set.of(
                new CommandNetStatGroup(pluginNetStat, pluginDatabase),
                new CommandNetStatServer(pluginNetStat, pluginDatabase)
        ));
    }

}
