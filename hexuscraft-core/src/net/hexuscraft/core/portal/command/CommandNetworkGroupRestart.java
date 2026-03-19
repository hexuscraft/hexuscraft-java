package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupRestart extends BaseCommand<CorePortal> {

    public CommandNetworkGroupRestart(final CorePortal corePortal) {
        super(corePortal,
                "restart",
                "<Server Group>",
                "Restart all servers of a group.",
                Set.of("r",
                        "reboot",
                        "rb"),
                CorePortal.PERM.COMMAND_NETWORK_GROUP_RESTART);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (args[0].equals("*")) {
            sender.sendMessage(F.fMain(this,
                    "Sending restart command to all server groups..."));
            _miniPlugin.restartServerGroupAsync("*");
            sender.sendMessage(F.fMain(this,
                    F.fSuccess("Successfully sent restart command to all server groups.")));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final ServerGroupData serverGroupData;
            try {
                serverGroupData = _miniPlugin.getServerGroup(args[0]);
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError(
                                "JedisException while fetching server group punish. Please try again later or contact dev-ops if this issue persists.")));
                return;
            }

            if (serverGroupData == null) {
                sender.sendMessage(F.fMain(this,
                        F.fError("Could not locate server group with name ",
                                F.fItem(args[0]),
                                ".")));
                return;
            }

            _miniPlugin._hexusPlugin.runAsync(() -> {
                try {
                    _miniPlugin.restartServerGroupAsync(serverGroupData._name);
                } catch (final JedisException ex) {
                    sender.sendMessage(F.fMain(this,
                            F.fError(
                                    "JedisException while restarting server group. Please try again later or contact dev-ops if this issue persists.")));
                    return;
                }
                sender.sendMessage(F.fMain(this,
                        "Restarting server group ",
                        F.fItem(serverGroupData._name),
                        "..."));
            });
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) return Arrays.asList(_miniPlugin.getServerGroupNames());
        return List.of();
    }

}
