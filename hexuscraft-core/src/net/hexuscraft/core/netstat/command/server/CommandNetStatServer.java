package net.hexuscraft.core.netstat.command.server;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.PluginNetStat;

import java.util.Set;

public class CommandNetStatServer extends BaseMultiCommand<HexusPlugin> {

    public CommandNetStatServer(PluginNetStat pluginNetStat, @SuppressWarnings("unused") final PluginDatabase pluginDatabase) {
        super(pluginNetStat, "server", "Manage servers.", Set.of("s"), PluginNetStat.PERM.COMMAND_NETSTAT_SERVER, Set.of());
    }

}
