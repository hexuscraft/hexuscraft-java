package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public final class CommandNetworkServerRestart extends BaseCommand<MiniPluginPortal> {

    public CommandNetworkServerRestart(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "restart", "<Server>",
                "The specified server will prevent new players from joining, send existing players to a Lobby, and then restart.",
                Set.of("r", "reboot", "rb"), MiniPluginPortal.PERM.COMMAND_NETWORK_SERVER_RESTART);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final ServerData serverData;
            try {
                serverData = _miniPlugin.getServerDataFromName(args[0]);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching server data. Please try again later or contact dev-ops if this issue persists.")));
                return;
            }

            if (serverData == null) {
                sender.sendMessage(
                        F.fMain(this, F.fError("Could not locate server with name ", F.fItem(args[0]), ".")));
                return;
            }

            _miniPlugin._hexusPlugin.runAsync(() -> {
                try {
                    _miniPlugin.restartServerYields(serverData._name);
                } catch (final JedisException ex) {
                    sender.sendMessage(F.fMain(this, F.fError(
                            "JedisException while restarting server. Please try again later or contact dev-ops if this issue persists.")));
                    return;
                }
                sender.sendMessage(F.fMain(this, "Restarting server ", F.fItem(serverData._name), "..."));
            });
        });
    }


}
