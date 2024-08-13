package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupDelete extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupDelete(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "delete", "<Name>", "Delete a server group.", Set.of("del", "d"), MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_DELETE);
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
