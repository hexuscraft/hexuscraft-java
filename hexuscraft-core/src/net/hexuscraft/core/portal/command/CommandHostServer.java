package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CommandHostServer extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandHostServer(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "hostserver", "", "Start a private server or teleport to your existing server.", Set.of("hps", "mps", "hosthps", "hostmps"), MiniPluginPortal.PERM.COMMAND_HOSTSERVER);

        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final String serverGroupName = "_" + sender.getName();

        final ServerData existingServerData = _miniPlugin._serverCache.stream().filter(serverData -> serverData._group.equals(serverGroupName)).findAny().orElse(null);
        if (existingServerData != null) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                return;
            }

            if (existingServerData._name.equals(_miniPlugin._serverName)) {
                sender.sendMessage(F.fMain(this, F.fError("You are already connected to ", F.fItem(_miniPlugin._serverName), ".")));
                return;
            }

            if (existingServerData._updatedByMonitor) {
                sender.sendMessage(F.fMain(this, F.fError("We found a server with name ", F.fItem(existingServerData._name), " but it has not finished starting yet. Perhaps try again in a few moments?")));
                return;
            }

            _miniPlugin.teleport(player, existingServerData._name);
            return;
        }

        final ServerGroupData existingServerGroupData = _miniPlugin._serverGroupCache.stream().filter(serverGroupData -> serverGroupData._name.equals(serverGroupName)).findAny().orElse(null);
        if (existingServerGroupData != null) {
            sender.sendMessage(F.fMain(this, "Your server is currently being created. You will be teleported shortly."));
            return;
        }

        Set<Integer> portsInUse = _miniPlugin._serverGroupCache.stream().filter(serverGroupData -> serverGroupData._name.startsWith("_")).map(serverGroupData -> serverGroupData._minPort).collect(Collectors.toUnmodifiableSet());
        if (portsInUse.size() > MiniPluginPortal.MAX_PORT_PRIVATE_SERVERS - MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS) {
            sender.sendMessage(F.fMain(this, "Sorry, but we are currently at maximum capacity for private servers. Please try again later."));
            return;
        }

        int portRange = MiniPluginPortal.MAX_PORT_PRIVATE_SERVERS - MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS + 1;

        boolean[] used = new boolean[portRange];
        for (int p : portsInUse) {
            if (p >= MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS && p <= MiniPluginPortal.MAX_PORT_PRIVATE_SERVERS)
                used[p - MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS] = true;
        }

        // collect free ports into an int[] via streams
        int[] free = IntStream.range(0, portRange)
                .filter(i -> !used[i])
                .map(i -> MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS + i)
                .toArray();

        if (free.length == 0) {
            sender.sendMessage(F.fMain(this, "Sorry, but we are currently at maximum capacity for private servers. Please try again later."));
            return;
        }
        int port = free[ThreadLocalRandom.current().nextInt(free.length)];

        _miniPlugin._hexusPlugin.runAsync(() -> {
            try {
                new ServerGroupData(serverGroupName, PermissionGroup.MEMBER.name(), port, port, 1, 0, "Arcade.jar", "Arcade.zip", 512, 100, false, 10000, new String[]{"SURVIVAL_GAMES"}, sender instanceof final Player player ? player.getUniqueId() : UtilUniqueId.EMPTY_UUID).update(_miniPluginDatabase.getUnifiedJedis());
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError("There was an error creating your server. Please try again in a few moments or contact an administrator if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this, F.fSuccess("Successfully created your server. You will be automatically teleported once your server has started. This may take up to 30 seconds.")));
        });
    }
}
