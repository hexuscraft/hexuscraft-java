package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public final class CommandNetworkServerList extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkServerList(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "list", "", "List all servers.", Set.of("l"),
                MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_LIST);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final String[] serverNames;
            try {
                serverNames = Arrays.stream(ServerQueries.getServers(_miniPluginDatabase.getUnifiedJedis()))
                        .map(serverData -> serverData._name).sorted(Comparator.comparing(String::toLowerCase))
                        .toArray(String[]::new);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching server names. Please contact dev-ops if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this, "Servers: ", F.fItem(serverNames)));
        });
    }

}
