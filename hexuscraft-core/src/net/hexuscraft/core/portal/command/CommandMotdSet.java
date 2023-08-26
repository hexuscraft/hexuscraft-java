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

public class CommandMotdSet extends BaseCommand {

    PluginDatabase _pluginDatabase;

    CommandMotdSet(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal, "set", "<Message>", "Set the current MOTD.", Set.of("s"), PluginPortal.PERM.COMMAND_MOTD_SET);

        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        sender.sendMessage(F.fMain(this) + "Updating the MOTD...");
        _pluginDatabase.getJedisPooled().set(ServerQueries.SERVERS_MOTD(), message);
        sender.sendMessage(F.fMain(this) + "Updated the MOTD:\n" + F.fMain(C.cDGray + C.fBold) + C.fReset + message + "\n" + F.fMain(this) + "It may take a few seconds for all proxies to update.");
    }

}
