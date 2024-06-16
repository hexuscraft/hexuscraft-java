package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.network.PluginNetwork;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupList extends BaseCommand<HexusPlugin> {

    private final PluginDatabase _pluginDatabase;

    CommandNetworkGroupList(final PluginNetwork pluginNetwork, final PluginDatabase pluginDatabase) {
        super(pluginNetwork, "list", "", "List all server groups.", Set.of("l"), PluginNetwork.PERM.COMMAND_NETSTAT_GROUP_LIST);
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
