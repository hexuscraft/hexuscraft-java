package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.network.PluginNetwork;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupDelete extends BaseCommand<HexusPlugin> {

    private final PluginDatabase _pluginDatabase;

    CommandNetworkGroupDelete(final PluginNetwork pluginNetwork, final PluginDatabase pluginDatabase) {
        super(pluginNetwork, "delete", "<Name>", "Delete a server group.", Set.of("del", "d"), PluginNetwork.PERM.COMMAND_NETSTAT_GROUP_DELETE);
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
