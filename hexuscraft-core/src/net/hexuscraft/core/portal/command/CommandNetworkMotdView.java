package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkMotdView extends BaseCommand<CorePortal>
{

    private final CoreDatabase _coreDatabase;

    CommandNetworkMotdView(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal, "view", "", "View the current MOTD.", Set.of("v"), CorePortal.PERM.COMMAND_MOTD_VIEW);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {
        sender.sendMessage(F.fMain(this,
                                   "Viewing the current MOTD:\n",
                                   F.fSub("",
                                          C.fReset +
                                          ChatColor.translateAlternateColorCodes('&',
                                                                                 ServerQueries.getMotd(_coreDatabase._database._jedis)))));
    }

}
