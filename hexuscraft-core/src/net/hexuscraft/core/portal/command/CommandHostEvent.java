package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
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
            final ServerData[] serverData;
            try {
                serverData = ServerQueries.getServers(_miniPluginDatabase.getUnifiedJedis(), "EVENT");
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while fetching existing server data. Contact dev-ops or event-lead if this issue persists.")));
                return;
            }

            if (serverData.length > 0) {
                if (!(sender instanceof final Player player)) {
                    sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                    return;
                }

                if (serverData[0]._name.equals(_miniPlugin._serverName)) {
                    sender.sendMessage(F.fMain(this,
                            F.fError("You are already connected to ", F.fItem(_miniPlugin._serverName), ".")));
                    return;
                }

                _miniPlugin.teleportAsync(player, serverData[0]._name);
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
                        "There was an error creating your server data. Please try again later or contact a staff member if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this, F.fSuccess("Successfully created your server group data."),
                    " You will be automatically teleported in a few moments."));
        });
    }
}
