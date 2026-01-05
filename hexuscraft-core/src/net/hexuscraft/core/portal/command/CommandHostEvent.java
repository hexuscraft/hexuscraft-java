package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CommandHostEvent extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandHostEvent(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "hostevent", "", "Start the event server or teleport to the existing event server.", Set.of("hes", "mes", "hosthes", "hostmes"), MiniPluginPortal.PERM.COMMAND_HOSTSERVER);

        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        final String serverGroupName = "EVENT";

        final ServerData[] existingServers = _miniPlugin.getServers(serverGroupName);
        if (existingServers.length > 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                return;
            }

            _miniPlugin.teleport(player, existingServers[0]._name);
            return;
        }

        if (_miniPlugin.getServerGroup(serverGroupName) != null) {
            sender.sendMessage(F.fMain(this, "Your server is currently being created. You will be teleported shortly."));
            return;
        }

        final Set<Integer> portsInUse = Arrays.stream(_miniPlugin.getServerGroups()).filter(serverGroupData -> serverGroupData._name.startsWith("_")).map(serverGroupData -> serverGroupData._minPort).collect(Collectors.toUnmodifiableSet());
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
        int[] free = IntStream.range(0, portRange).filter(i -> !used[i]).map(i -> MiniPluginPortal.MIN_PORT_PRIVATE_SERVERS + i).toArray();

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
