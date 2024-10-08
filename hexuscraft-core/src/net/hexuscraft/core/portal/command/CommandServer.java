package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPooled;

import java.util.Set;

public final class CommandServer extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandServer(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal,
                "server",
                "[Name]",
                "View your current server or teleport to a server.",
                Set.of("sv", "portal"),
                MiniPluginPortal.PERM.COMMAND_SERVER);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(F.fMain(this) + "Only players can teleport to a server.");
                return;
            }
            final String serverName = args[0];

            if (_miniPlugin._serverName.equals(serverName)) {
                sender.sendMessage(F.fMain(this)
                        + "You are already connected to " + F.fItem(serverName) + ".");
                return;
            }

            final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();

            final ServerData serverData = ServerQueries.getServer(jedis, serverName);
            if (serverData == null) {
                sender.sendMessage(F.fMain(this)
                        + F.fError("Could not locate server with name ", F.fItem(serverName), "."));
                return;
            }

            final ServerGroupData serverGroupData = ServerQueries.getServerGroup(jedis, serverData._group);
            if (serverGroupData == null) {
                sender.sendMessage(F.fMain(this)
                        + F.fError("Could not locate servergroup with name ", F.fItem(serverData._group)));
                return;
            }

            if (serverGroupData._requiredPermission != null
                    && !serverGroupData._requiredPermission.isEmpty()
                    && !sender.hasPermission(serverGroupData._requiredPermission)) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            _miniPlugin.teleport(sender.getName(), serverName);
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(F.fMain(this) + "You are connected to " + F.fItem(_miniPlugin._serverName) + ".");
            return;
        }
        sender.sendMessage(help(alias));
    }
}
