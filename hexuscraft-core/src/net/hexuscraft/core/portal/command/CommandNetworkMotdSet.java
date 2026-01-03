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

public final class CommandNetworkMotdSet extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkMotdSet(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "set", "<Message>", "Set the current MOTD.", Set.of("s"),
                MiniPluginPortal.PERM.COMMAND_MOTD_SET);

        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        final String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
        sender.sendMessage(F.fMain(this, "Please wait... Updating the MOTD to:\n", C.fReset + message));

        _miniPlugin._hexusPlugin.runAsync(() -> {
            ServerQueries.setMotd(_miniPluginDatabase.getUnifiedJedis(), message);
            _miniPlugin._hexusPlugin.runSync(() -> sender.sendMessage(
                    F.fMain(this, F.fSuccess("Successfully updated the MOTD:\n"), C.fReset + message) + "\n" +
                            F.fMain("", "It may take a few seconds for all proxies to update.")));
        });

    }

}
