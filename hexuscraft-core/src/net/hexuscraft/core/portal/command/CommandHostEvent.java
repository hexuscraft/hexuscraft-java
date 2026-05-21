package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public class CommandHostEvent extends BaseCommand<CorePortal> {

    final CoreDatabase _coreDatabase;

    public CommandHostEvent(CorePortal corePortal, CoreDatabase coreDatabase) {
        super(corePortal,
                "hostevent",
                "",
                "Start the event server or teleport to the existing event server.",
                Set.of("hes", "mes", "hosthes", "hostmes"),
                CorePortal.PERM.COMMAND_HOSTEVENT);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        String serverGroupName = "Event";

        ServerData[] existingServers = _miniPlugin.getServers(serverGroupName);
        if (existingServers.length > 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to the event server.")));
                return;
            }

            _miniPlugin.teleport(player, existingServers[0]._name);
            return;
        }

        if (_miniPlugin.getServerGroup(serverGroupName) != null) {
            sender.sendMessage(F.fMain(this,
                    "An event server is currently being created. You will be teleported shortly."));
            return;
        }

        ServerGroupData.Builder builder = new ServerGroupData.Builder().name(serverGroupName)
                .capacity(100)
                .games(new GameType[]{GameType.SURVIVAL_GAMES})
                .maxPort(CorePortal.EVENT_SERVER_PORT)
                .minPort(CorePortal.EVENT_SERVER_PORT)
                .plugin("Arcade.jar")
                .totalServers(1)
                .worldZip("Arcade.zip");

        if (sender instanceof Player player) {
            builder.hostUUID(player.getUniqueId());
        }

        ServerGroupData serverGroupData = builder.build();

        _miniPlugin._hexusPlugin.runAsync(() ->
        {
            try {
                serverGroupData.update(_coreDatabase._database._jedis);
            } catch (JedisException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("There was an error creating an event server. Please try again later or contact an " +
                                "administrator if this issue persists.")));
                return;
            }
            sender.sendMessage(F.fMain(this,
                    F.fSuccess("Successfully created an event server. You will be automatically teleported once your " +
                            "server " +
                            "has started. This may take up to 30 seconds.")));
        });
    }
}
