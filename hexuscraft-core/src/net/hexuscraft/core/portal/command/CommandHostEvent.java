package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class CommandHostEvent extends BaseCommand {

    private final PluginDatabase _pluginDatabase;

    private final Set<CommandSender> _pending;

    public CommandHostEvent(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal, "hostevent", "", "Start a new private server.", Set.of("hes"), PluginPortal.PERM.COMMAND_HOSTEVENT);

        _pluginDatabase = pluginDatabase;
        _pending = new HashSet<>();
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (_pending.contains(sender)) {
            sender.sendMessage(F.fMain(this, F.fError("You already have a pending request! Please wait until your previous request has completed...")));
            return;
        }
        _pending.add(sender);

        final BukkitScheduler scheduler = _miniPlugin._plugin.getServer().getScheduler();
        sender.sendMessage(F.fMain(this, "Searching for existing servers..."));

        scheduler.runTaskAsynchronously(_miniPlugin._plugin, () -> {
            final JedisPooled jedis = _pluginDatabase.getJedisPooled();

            final String serverName;
            try {
                serverName = ((Callable<String>) () -> {
                    for (ServerData serverData : ServerQueries.getServers(jedis))
                        if (serverData._group.split("-", 2)[0].equals("EVENT"))
                            return serverData._name;
                    return null;
                }).call();
            } catch (Exception e) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("There was an error fetching existing server data. Maybe try again later?")));
                return;
            }

            if (serverName != null) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("There is already an event server!\n"), F.fMain("", "Connect to it with ", F.fItem("/server " + serverName + "."))));
                return;
            }

            try {
                sender.sendMessage(F.fMain(this, "Creating server group..."));
                final ServerGroupData groupData = new ServerGroupData("EVENT", PermissionGroup.MEMBER.name(),
                        30050, 30051, 1, 0,
                        "Arcade.jar", "Lobby_Arcade.zip", 512, 40, false);
                groupData.update(jedis);
                sender.sendMessage(F.fMain(this, "Waiting for your server to start..."));
            } catch (Exception e) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("There was an error performing your request. Maybe try again later?")));
                return;
            }

            final BukkitTask[] tasks = new BukkitTask[1]; // a bit of a dodgy workaround - but it works
            final long start = System.currentTimeMillis();
            tasks[0] = scheduler.runTaskTimerAsynchronously(_miniPlugin._plugin, () -> {
                if (System.currentTimeMillis() - start > 30000) {
                    sender.sendMessage(F.fMain(this, F.fError("Could not locate your server within 30 seconds. There might not be enough resources available to start your server. Maybe try again later?")));
                    _pending.remove(sender);
                    tasks[0].cancel();
                    return;
                }

                for (ServerData serverData : ServerQueries.getServers(jedis, new ServerGroupData("EVENT", Map.of()))) {
                    scheduler.runTaskLaterAsynchronously(_miniPlugin._plugin, () -> ((PluginPortal) _miniPlugin).teleport(sender.getName(), serverData._name), 20L);
                    _pending.remove(sender);
                    tasks[0].cancel();
                    return;
                }
            }, 100L, 20L);
        });
    }

}
