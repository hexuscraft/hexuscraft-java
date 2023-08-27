package net.hexuscraft.core.netstat.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.netstat.PluginNetStat;

import java.util.Set;

public class CommandNetStatGroup extends BaseMultiCommand {

    CommandNetStatGroup(PluginNetStat pluginNetStat) {
        super(pluginNetStat, "group", "Manage server groups.", Set.of("g"), PluginNetStat.PERM.COMMAND_NETSTAT_GROUP, Set.of());
    }

}
