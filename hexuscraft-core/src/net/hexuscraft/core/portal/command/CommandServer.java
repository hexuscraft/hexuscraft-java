package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPooled;

import java.util.Set;

public class CommandServer extends BaseCommand {

    private final PluginPortal _portal;
    private final PluginDatabase _database;

    public CommandServer(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal,
                "server",
                "[Name]",
                "View your current server or teleport to a server.",
                Set.of("sv", "portal"),
                PluginPortal.PERM.COMMAND_SERVER);
        _portal = pluginPortal;
        _database = pluginDatabase;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(F.fMain(this) + "Only players can teleport to a server.");
                return;
            }
            final String serverName = args[0];

            if (_portal._serverName.equals(serverName)) {
                sender.sendMessage(F.fMain(this)
                        + "You are already connected to " + F.fItem(serverName) + ".");
                return;
            }

            final JedisPooled jedis = _database.getJedisPooled();

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

            _portal.teleport(sender.getName(), serverName);
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(F.fMain(this) + "You are connected to " + F.fItem(_portal._serverName) + ".");
            return;
        }
        sender.sendMessage(help(alias));
    }
}
