package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Set;

public final class CommandServer extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandServer(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "server", "[Name]", "View your current server or teleport to a server.",
                Set.of("sv", "portal"), MiniPluginPortal.PERM.COMMAND_SERVER);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this) + "Only players can teleport to a server.");
                return;
            }
            final String serverName = args[0];

            if (_miniPlugin._serverName.equals(serverName)) {
                sender.sendMessage(F.fMain(this,
                        F.fError("You are already connected to ", F.fItem(_miniPlugin._serverName), ".")));
                return;
            }

            final UnifiedJedis jedis = _miniPluginDatabase.getUnifiedJedis();

            final ServerData serverData = ServerQueries.getServer(jedis, serverName);
            if (serverData == null) {
                sender.sendMessage(
                        F.fMain(this) + F.fError("Could not locate server with name ", F.fItem(serverName), "."));
                return;
            }
            if (serverData._updatedByMonitor) {
                sender.sendMessage(F.fMain(this, F.fError("We found a server with name ", F.fItem(serverName),
                        " but it has not finished starting yet. Perhaps try again in a few moments?")));
                return;
            }

            final ServerGroupData serverGroupData = ServerQueries.getServerGroup(jedis, serverData._group);
            if (serverGroupData == null) {
                sender.sendMessage(F.fMain(this) +
                        F.fError("Could not locate servergroup with name ", F.fItem(serverData._group)));
                return;
            }

            if (serverGroupData._requiredPermission != null && !serverGroupData._requiredPermission.isEmpty() &&
                    !sender.hasPermission(serverGroupData._requiredPermission)) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            _miniPlugin.teleportAsync(player, serverName);
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(F.fMain(this, "You are connected to ", F.fItem(_miniPlugin._serverName), "."));
            return;
        }
        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) return _miniPlugin._serverCache.stream().map(serverData -> serverData._name).toList();
        return List.of();
    }
}
