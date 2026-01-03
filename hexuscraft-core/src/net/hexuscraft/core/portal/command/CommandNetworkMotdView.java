package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkMotdView extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkMotdView(MiniPluginPortal miniPluginPortal, MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "view", "", "View the current MOTD.", Set.of("v"),
                MiniPluginPortal.PERM.COMMAND_MOTD_VIEW);

        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        sender.sendMessage(F.fMain(this, "Viewing the current MOTD:\n", F.fSub("", C.fReset +
                ChatColor.translateAlternateColorCodes('&',
                        ServerQueries.getMotd(_miniPluginDatabase.getUnifiedJedis())))));
    }

}
