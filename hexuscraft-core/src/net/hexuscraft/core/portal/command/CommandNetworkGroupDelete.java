package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupDelete extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupDelete(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "delete", "<Name>", "Delete a server group.", Set.of("del", "d"),
                MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_DELETE);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final String key = ServerQueries.SERVERGROUP(args[0]);
        _miniPluginDatabase.getUnifiedJedis().del(key);
        sender.sendMessage(F.fMain(this, "Deleted ", F.fItem(key)));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return Arrays.stream(ServerQueries.getServerGroups(_miniPluginDatabase.getUnifiedJedis()))
                .map(serverGroupData -> serverGroupData._name).toList();
    }
}
