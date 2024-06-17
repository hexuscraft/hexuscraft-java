package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.MiniPluginNetwork;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupList extends BaseCommand<MiniPluginNetwork> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupList(final MiniPluginNetwork miniPluginNetwork, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNetwork, "list", "", "List all server groups.", Set.of("l"), MiniPluginNetwork.PERM.COMMAND_NETSTAT_GROUP_LIST);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();
        final List<String> serverGroupNames = new ArrayList<>(ServerQueries.getServerGroupsAsMap(jedis).keySet());

        sender.sendMessage(F.fMain(this, "Server Groups:\n", F.fList(serverGroupNames)));
    }

}
