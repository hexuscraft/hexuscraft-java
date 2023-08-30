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
import org.bukkit.scheduler.BukkitScheduler;
import redis.clients.jedis.JedisPooled;

import java.util.*;
import java.util.concurrent.Callable;

public class CommandHostEvent extends BaseCommand {

    PluginDatabase _pluginDatabase;

    Set<Player> _pending;

    public CommandHostEvent(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        //noinspection SpellCheckingInspection
        super(pluginPortal, "hostevent", "", "Start a new private server.", Set.of("eventserver", "hes", "mes"), PluginPortal.PERM.COMMAND_HOSTEVENT);

        _pending = new HashSet<>();
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create private servers. Maybe try " + F.fItem("/hostevent") + " instead?");
            return;
        }

        if (_pending.contains(player)) {
            player.sendMessage(F.fMain(this) + F.fError("You already have a pending request! Please wait until your previous request has completed..."));
            return;
        }

        _pending.add(player);

        BukkitScheduler scheduler = _miniPlugin._javaPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(_miniPlugin._javaPlugin, () -> {
            try {
                final JedisPooled jedis = _pluginDatabase.getJedisPooled();

                final ServerGroupData eventServerGroupData = ((Callable<ServerGroupData>) () -> {
                    for (UUID serverUuid : jedis.smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).toList()) {
                        ServerData serverData = new ServerData(serverUuid, jedis.hgetAll(ServerQueries.SERVER(serverUuid)));
                        ServerGroupData serverGroupData = new ServerGroupData(serverData._group, jedis.hgetAll(ServerQueries.SERVERGROUP(serverData._group)));
                        if (!serverGroupData._prefix.equalsIgnoreCase("event")) {
                            continue;
                        }
                        return serverGroupData;
                    }
                    return null;
                }).call();

                if (eventServerGroupData == null) {
                    player.sendMessage(F.fMain(this) + F.fError("An event server group does not exist on this database."));
                    return;
                }

                final List<ServerData> eventServers = new ArrayList<>();

                final String serverName = ((Callable<String>) () -> {
                    for (UUID serverUuid : jedis.smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).toList()) {
                        ServerData serverData = new ServerData(serverUuid, jedis.hgetAll(ServerQueries.SERVER(serverUuid)));
                        ServerGroupData serverGroupData = new ServerGroupData(serverData._group, jedis.hgetAll(ServerQueries.SERVERGROUP(serverData._group)));
                        if (!serverGroupData._uuid.equals(eventServerGroupData._uuid)) {
                            continue;
                        }
                        eventServers.add(serverData);
                        if (!serverData._host.equals(player.getUniqueId())) {
                            continue;
                        }
                        return serverData._name;
                    }
                    return null;
                }).call();

                scheduler.runTask(_miniPlugin._javaPlugin, () -> {
                    if (serverName != null) {
                        player.sendMessage(F.fMain(this) + "You are already the host of an event server!\n" + F.fMain() + "Connect to it with " + F.fItem("/server " + serverName + "."));
                        return;
                    }

                    final Map<Integer, ServerData> eventServerIdMap = new HashMap<>();
                    eventServers.forEach(serverData -> eventServerIdMap.put(Integer.parseInt(serverData._name.split("-", 2)[1]), serverData));

                    //noinspection ReassignedVariable
                    long newServerId = -1;
                    long totalPotentialServers = eventServerGroupData._maxPort - eventServerGroupData._minPort;
                    for (int i = 1; i < totalPotentialServers; i++) {
                        if (eventServerIdMap.containsKey(i)) {
                            continue;
                        }
                        newServerId = i;
                        break;
                    }

                    if (newServerId == -1) {
                        player.sendMessage(F.fMain(this) + "Sorry about this, but there are no available server slots remaining. Maybe try again later?");
                        return;
                    }

                    player.sendMessage(F.fMain(this) + F.fItem(eventServerGroupData._prefix + "-" + newServerId) + " is being created...\n" + F.fMain() + "You will be teleported in around " + F.fItem("30 Seconds") + ".");
                });
            } catch (Exception e) {
                scheduler.runTask(_miniPlugin._javaPlugin, () -> player.sendMessage(F.fMain(this) + F.fError("There was an error while contacting the database. Maybe try again later?")));
            } finally {
                _pending.remove(player);
            }
        });
    }

}
