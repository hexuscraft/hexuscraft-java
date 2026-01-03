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

public final class CommandNetworkGroupList extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupList(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "list", "", "List all server groups.", Set.of("l"),
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
            final String[] serverGroupNames;
            try {
                serverGroupNames = Arrays.stream(ServerQueries.getServerGroups(_miniPluginDatabase.getUnifiedJedis()))
                        .map(serverGroupData -> serverGroupData._name).sorted(Comparator.comparing(String::toLowerCase))
                        .toArray(String[]::new);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching server group names. Please contact dev-ops if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this, "Server Groups: ", F.fItem(serverGroupNames)));
        });
    }

}
