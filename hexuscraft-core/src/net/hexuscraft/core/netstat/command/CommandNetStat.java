package net.hexuscraft.core.netstat.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.netstat.PluginNetStat;

import java.util.Set;

public class CommandNetStat extends BaseMultiCommand {

    public CommandNetStat(PluginNetStat pluginNetStat) {
        super(pluginNetStat, "netstat", "Manage the network.", Set.of("net"), PluginNetStat.PERM.COMMAND_NETSTAT, Set.of(
                new CommandNetStatGroup(pluginNetStat),
                new CommandNetStatServer(pluginNetStat)
        ));
    }

}
