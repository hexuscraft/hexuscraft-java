package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
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

        if (args[0].equals("*")) {
            sender.sendMessage(F.fMain(this, "Sending restart command to all servers..."));
            _miniPlugin._serverCache.stream().filter(serverData -> !serverData._name.equals(_miniPlugin._serverName))
                    .map(serverData -> serverData._name).forEach(_miniPlugin::restartServerYields);
            _miniPlugin.restartServerYields(_miniPlugin._serverName);
            sender.sendMessage(F.fMain(this, F.fSuccess("Successfully sent restart command to all servers.")));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final ServerData serverData;
            try {
                serverData = _miniPlugin.getServerDataFromName(args[0]);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching server punish. Please try again later or contact dev-ops if this issue persists.")));
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

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1)
            return _miniPlugin._serverCache.stream().map(serverData -> serverData._name).toList();
        return List.of();
    }

}
