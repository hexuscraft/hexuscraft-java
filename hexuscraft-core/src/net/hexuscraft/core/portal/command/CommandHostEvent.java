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

public final class CommandHostEvent extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandHostEvent(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "hostevent", "", "Start the event server or teleport to the existing event server.",
                Set.of("hes", "mes", "hosthes", "hostmes"), MiniPluginPortal.PERM.COMMAND_HOSTSERVER);

        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final ServerData[] existingServerDatas =
                    _miniPlugin._serverCache.stream().filter(serverData -> serverData._group.equals("EVENT"))
                            .toArray(ServerData[]::new);
            if (existingServerDatas.length > 0) {
                if (!(sender instanceof final Player player)) {
                    sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                    return;
                }

                if (existingServerDatas[0]._name.equals(_miniPlugin._serverName)) {
                    sender.sendMessage(F.fMain(this,
                            F.fError("You are already connected to ", F.fItem(_miniPlugin._serverName), ".")));
                    return;
                }

                if (existingServerDatas[0]._updatedByMonitor) {
                    sender.sendMessage(F.fMain(this,
                            F.fError("We found a server with name ", F.fItem(existingServerDatas[0]._name),
                                    " but it has not finished starting yet. Perhaps try again in a few moments?")));
                    return;
                }

                _miniPlugin.teleport(player, existingServerDatas[0]._name);
                return;
            }

            final int port = ThreadLocalRandom.current().nextInt(50000, 51000);
            try {
                new ServerGroupData("EVENT", PermissionGroup.MEMBER.name(), port, port, 1, 0, "Arcade.jar",
                        "Arcade.zip", 512, 100, false, 10000, new String[]{"EVENT"},
                        sender instanceof final Player player ? player.getUniqueId() : UtilUniqueId.EMPTY_UUID).update(
                        _miniPluginDatabase.getUnifiedJedis());
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while creating server punish. Contact dev-ops if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this, F.fSuccess("Successfully created server group punish."),
                    " You will be automatically teleported in ~30 seconds."));
        });
    }
}
