package net.hexuscraft.core.netstat.command.group;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.PluginNetStat;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandNetStatGroupList extends BaseCommand {

    private final PluginDatabase _pluginDatabase;

    CommandNetStatGroupList(final PluginNetStat pluginNetStat, final PluginDatabase pluginDatabase) {
        super(pluginNetStat, "list", "", "List all server groups.", Set.of("l"), PluginNetStat.PERM.COMMAND_NETSTAT_GROUP_LIST);
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final JedisPooled jedis = _pluginDatabase.getJedisPooled();
        final List<String> serverGroupNames = new ArrayList<>(ServerQueries.getServerGroupsAsMap(jedis).keySet());

        sender.sendMessage(F.fMain(this, "Server Groups:\n", F.fList(serverGroupNames)));
    }

}
