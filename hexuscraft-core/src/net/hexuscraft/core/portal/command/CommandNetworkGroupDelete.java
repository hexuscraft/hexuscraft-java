package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupDelete extends BaseCommand<CorePortal> {

    private final CoreDatabase _coreDatabase;

    CommandNetworkGroupDelete(final CorePortal corePortal, final CoreDatabase coreDatabase) {
        super(corePortal,
                "delete",
                "<Name>",
                "Delete a server group.",
                Set.of("del",
                        "d"),
                CorePortal.PERM.COMMAND_NETWORK_GROUP_DELETE);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final String key = ServerQueries.SERVERGROUP(args[0]);
        _coreDatabase._database._jedis.del(key);
        sender.sendMessage(F.fMain(this,
                "Deleted ",
                F.fItem(key)));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1)
            return Arrays.asList(_miniPlugin.getServerGroupNames());
        return List.of();
    }

}
