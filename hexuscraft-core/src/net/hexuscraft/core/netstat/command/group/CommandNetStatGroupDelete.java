package net.hexuscraft.core.netstat.command.group;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.PluginNetStat;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandNetStatGroupDelete extends BaseCommand {

    private final PluginDatabase _pluginDatabase;

    CommandNetStatGroupDelete(final PluginNetStat pluginNetStat, final PluginDatabase pluginDatabase) {
        super(pluginNetStat, "delete", "<Name>", "Delete a server group.", Set.of("del", "d"), PluginNetStat.PERM.COMMAND_NETSTAT_GROUP_DELETE);
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final JedisPooled jedis = _pluginDatabase.getJedisPooled();
        final String key = ServerQueries.SERVERGROUP(args[0]);
        sender.sendMessage(F.fMain(this, "Deleted ", F.fItem(key)));
        jedis.del(key);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return Arrays.stream(ServerQueries.getServerGroups(_pluginDatabase.getJedisPooled())).map(serverGroupData -> serverGroupData._name).toList();
    }
}
