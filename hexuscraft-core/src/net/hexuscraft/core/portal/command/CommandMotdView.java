package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandMotdView extends BaseCommand {

    private final PluginDatabase _pluginDatabase;

    CommandMotdView(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal, "view", "", "View the current MOTD.", Set.of("v"), PluginPortal.PERM.COMMAND_MOTD_VIEW);

        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        sender.sendMessage(F.fMain(this) + "Viewing the current MOTD:\n"
                + F.fMain(C.cDGray + C.fBold) + C.fReset + ChatColor.translateAlternateColorCodes('&', ServerQueries.getMotd(_pluginDatabase.getJedisPooled()))
        );
    }

}
