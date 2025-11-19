package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.Set;

public final class CommandNetworkServerList extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkServerList(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "list", "", "List all servers.", Set.of("l"), MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_LIST);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this, "Fetching server names..."));

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final JedisPooled redis = _miniPluginDatabase.getJedisPooled();
            final String[] serverNames = Arrays.stream(ServerQueries.getServers(redis)).map(serverData -> serverData._name).toArray(String[]::new);
            sender.sendMessage(F.fMain(this, "Servers: ", F.fList(serverNames)));
        });
    }

}
