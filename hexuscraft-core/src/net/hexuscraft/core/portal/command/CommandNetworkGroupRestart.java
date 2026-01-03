package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public final class CommandNetworkGroupRestart extends BaseCommand<MiniPluginPortal> {

    public CommandNetworkGroupRestart(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal, "restart", "<Server Group>", "Restart all servers of a group.",
                Set.of("r", "reboot", "rb"), MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_RESTART);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final ServerGroupData serverGroupData;
            try {
                serverGroupData = _miniPlugin.getServerGroupDataFromName(args[0]);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching server group data. Please try again later or contact dev-ops if this issue persists.")));
                return;
            }

            if (serverGroupData == null) {
                sender.sendMessage(
                        F.fMain(this, F.fError("Could not locate server group with name ", F.fItem(args[0]), ".")));
                return;
            }

            _miniPlugin._hexusPlugin.runAsync(() -> {
                try {
                    _miniPlugin.restartServerGroupYields(serverGroupData._name);
                } catch (final JedisException ex) {
                    sender.sendMessage(F.fMain(this, F.fError(
                            "JedisException while restarting server group. Please try again later or contact dev-ops if this issue persists.")));
                    return;
                }
                sender.sendMessage(F.fMain(this, "Restarting server group ", F.fItem(serverGroupData._name), "..."));
            });
        });
    }

}
