package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandMotdSet extends BaseCommand<HexusPlugin> {

    private final PluginDatabase _pluginDatabase;

    CommandMotdSet(final PluginPortal pluginPortal, final PluginDatabase pluginDatabase) {
        super(pluginPortal, "set", "<Message>", "Set the current MOTD.", Set.of("s"), PluginPortal.PERM.COMMAND_MOTD_SET);

        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        sender.sendMessage(F.fMain(this) + "Updating the MOTD...");
        final String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        ServerQueries.setMotd(_pluginDatabase.getJedisPooled(), message);
        sender.sendMessage(F.fMain(this) + "Updated the MOTD:\n" + F.fMain(C.cDGray + C.fBold) + C.fReset + message + "\n"
                + F.fMain(this) + "It may take a few seconds for all proxies to update.");
    }

}
