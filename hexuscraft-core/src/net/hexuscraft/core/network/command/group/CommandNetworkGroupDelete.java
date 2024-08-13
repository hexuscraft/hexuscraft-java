package net.hexuscraft.core.network.command.group;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.network.MiniPluginNetwork;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupDelete extends BaseCommand<MiniPluginNetwork> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupDelete(final MiniPluginNetwork miniPluginNetwork, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNetwork, "delete", "<Name>", "Delete a server group.", Set.of("del", "d"), MiniPluginNetwork.PERM.COMMAND_NETWORK_GROUP_DELETE);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();
        final String key = ServerQueries.SERVERGROUP(args[0]);
        sender.sendMessage(F.fMain(this, "Deleted ", F.fItem(key)));
        jedis.del(key);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return Arrays.stream(ServerQueries.getServerGroups(_miniPluginDatabase.getJedisPooled())).map(serverGroupData -> serverGroupData._name).toList();
    }
}
