package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandHostEvent extends BaseCommand {

    PluginDatabase _pluginDatabase;

    Set<Player> _pending;

    public CommandHostEvent(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        //noinspection SpellCheckingInspection
        super(pluginPortal, "hostevent", "", "Start a new private server.", Set.of("privateserver", "hps", "mps"), PluginPortal.PERM.COMMAND_HOSTEVENT);

        _pending = new HashSet<>();
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create private servers. Maybe try " + F.fItem("/hostevent") + " instead?");
            return;
        }

        if (_pending.contains(player)) {
            player.sendMessage(F.fMain(this) + "You already have a pending request! Please wait until your previous request has completed...");
            return;
        }

        _pending.add(player);

        for (UUID serverUuid : _pluginDatabase.getJedisPooled().smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).toList()) {
            Map<String, String> serverData = _pluginDatabase.getJedisPooled().hgetAll(ServerQueries.SERVER(serverUuid));
            String serverName = serverData.get("name");
            if (serverName.split("-")[0].equals("EVENT")) {
                player.sendMessage(F.fMain(this) + "An event server has already been created.\n" + F.fMain() + "Connect to it with " + F.fItem("/server " + serverName) + ".");
                _pending.remove(player);
                return;
            }
            if (serverData.containsKey("host") && serverData.get("host").equalsIgnoreCase(player.getUniqueId().toString())) {
                player.sendMessage(F.fMain(this) + "You are already the host of a server!\n" + F.fMain() + "Connect to it with " + F.fItem("/server " + serverName) + ".");
                _pending.remove(player);
                return;
            }
        }

        player.sendMessage(F.fMain(this) + F.fItem("EVENT-1") + " is being created...\n" + F.fMain() + "You will be teleported in around " + F.fItem("30 Seconds") + ".");

        _pending.remove(player);
    }

}
