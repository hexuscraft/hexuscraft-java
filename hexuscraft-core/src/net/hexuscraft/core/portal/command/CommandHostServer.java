package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommandHostServer extends BaseCommand<CorePortal>
{

    private final CoreDatabase _coreDatabase;

    public CommandHostServer(CorePortal corePortal, CoreDatabase coreDatabase)
    {
        super(corePortal,
                "hostserver",
                "",
                "Start a private server or teleport to your existing server.",
                Set.of("hps", "mps", "hosthps", "hostmps"),
                CorePortal.PERM.COMMAND_HOSTSERVER);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        String serverGroupName = "_" + sender.getName();

        ServerData[] existingServers = _miniPlugin.getServers(serverGroupName);
        if (existingServers.length > 0)
        {
            if (!(sender instanceof Player player))
            {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                return;
            }

            _miniPlugin.teleport(player, existingServers[0]._name);
            return;
        }

        if (_miniPlugin.getServerGroup(serverGroupName) != null)
        {
            sender.sendMessage(F.fMain(this,
                    "Your server is currently being created. You will be teleported shortly."));
            return;
        }

        Set<Integer> portsInUse = Arrays.stream(_miniPlugin.getServerGroups())
                .filter(serverGroupData -> serverGroupData._name.startsWith("_"))
                .map(serverGroupData -> serverGroupData._minPort)
                .collect(Collectors.toUnmodifiableSet());
        if (portsInUse.size() > CorePortal.MAX_PORT_PRIVATE_SERVERS - CorePortal.MIN_PORT_PRIVATE_SERVERS)
        {
            sender.sendMessage(F.fMain(this,
                    "Sorry, but we are currently at maximum capacity for private servers. Please try again later."));
            return;
        }

        int portRange = CorePortal.MAX_PORT_PRIVATE_SERVERS - CorePortal.MIN_PORT_PRIVATE_SERVERS + 1;
        boolean[] used = new boolean[portRange];
        for (int p : portsInUse)
        {
            if (p >= CorePortal.MIN_PORT_PRIVATE_SERVERS && p <= CorePortal.MAX_PORT_PRIVATE_SERVERS)
            {
                used[p - CorePortal.MIN_PORT_PRIVATE_SERVERS] = true;
            }
        }
        int[] free = IntStream.range(0, portRange)
                .filter(i -> !used[i])
                .map(i -> CorePortal.MIN_PORT_PRIVATE_SERVERS + i)
                .toArray();

        if (free.length == 0)
        {
            sender.sendMessage(F.fMain(this,
                    "Sorry, but we are currently at maximum capacity for private servers. Please try again later."));
            return;
        }

        int port = free[ThreadLocalRandom.current().nextInt(free.length)];
        _miniPlugin._hexusPlugin.runAsync(() ->
        {
            try
            {
                new ServerGroupData(serverGroupName,
                        PermissionGroup._PLAYER,
                        port,
                        port,
                        1,
                        0,
                        "Arcade.jar",
                        "Arcade.zip",
                        2048,
                        100,
                        false,
                        10000,
                        new GameType[]{GameType.SURVIVAL_GAMES},
                        sender instanceof Player player ? player.getUniqueId() : UtilUniqueId.EMPTY_UUID).update(
                        _coreDatabase._database._jedis);
            }
            catch (JedisException ex)
            {
                sender.sendMessage(F.fMain(this,
                        F.fError("There was an error creating your server. Please try again later or contact an " +
                                "administrator if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this,
                    F.fSuccess(
                            "Successfully created your server. You will be automatically teleported once your server " +
                                    "has started. This may take up to 30 seconds.")));
        });
    }
}
