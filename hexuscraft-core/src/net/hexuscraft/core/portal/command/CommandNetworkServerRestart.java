package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandNetworkServerRestart extends BaseCommand<MiniPluginPortal> {

    public CommandNetworkServerRestart(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "restart", "<Server>", "The specified server will prevent new players from joining, send existing players to a Lobby, and then restart.", Set.of("r", "reboot", "rb"), MiniPluginPortal.PERM.COMMAND_NETWORK_SERVER_RESTART);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this, "Locating server ", F.fItem(args[0]), "... ", C.fMagic + "..."));
        _miniPlugin._hexusPlugin.runAsync(() -> {
            if (_miniPlugin.getServerDataFromName(args[0]) == null) {
                _miniPlugin._hexusPlugin.runSync(() -> sender.sendMessage(F.fMain(this, F.fError("Could not locate server with name ", F.fItem(args[0]), "."))));
                return;
            }

            _miniPlugin._hexusPlugin.runSync(() -> sender.sendMessage(F.fMain(this, F.fSuccess("Successfully located server ", F.fItem(args[0]), "."), " Sending restart command... ", C.fMagic + "...")));
            _miniPlugin.restartServer(args[0]);
            _miniPlugin._hexusPlugin.runSync(() -> sender.sendMessage(F.fMain(this, F.fSuccess("Successfully sent restart command to server ", F.fItem(args[0]), "."), " The server will automatically transfer players to the lobby")));
        });
    }


}
