package net.hexuscraft.core.netstat.command.group;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.PluginNetStat;

import java.util.Set;

public class CommandNetStatGroup extends BaseMultiCommand<HexusPlugin> {

    public CommandNetStatGroup(final PluginNetStat pluginNetStat, final PluginDatabase pluginDatabase) {
        super(pluginNetStat, "group", "Manage server groups.", Set.of("g"), PluginNetStat.PERM.COMMAND_NETSTAT_GROUP, Set.of(
                new CommandNetStatGroupCreate(pluginNetStat, pluginDatabase),
                new CommandNetStatGroupDelete(pluginNetStat, pluginDatabase),
                new CommandNetStatGroupList(pluginNetStat, pluginDatabase)
        ));
    }

}
