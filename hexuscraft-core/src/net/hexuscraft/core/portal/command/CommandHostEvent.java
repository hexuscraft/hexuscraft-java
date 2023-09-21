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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.util.HashSet;
import java.util.Set;

public class CommandHostEvent extends BaseCommand {

    private final PluginDatabase _pluginDatabase;
    private final PluginPortal _pluginPortal;

    private final Set<Player> _pending;

    public CommandHostEvent(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal, "hostevent", "", "Start a new private server.", Set.of("hes"), PluginPortal.PERM.COMMAND_HOSTEVENT);

        _pending = new HashSet<>();
        _pluginDatabase = pluginDatabase;
        _pluginPortal = pluginPortal;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create private servers.");
            return;
        }

        if (_pending.contains(player)) {
            player.sendMessage(F.fMain(this) + F.fError("Please wait around 30 seconds before using this command again."));
            return;
        }

        _pending.add(player);

        final BukkitScheduler scheduler = _miniPlugin._javaPlugin.getServer().getScheduler();
        scheduler.runTaskLater(_miniPlugin._javaPlugin, () -> _pending.remove(player), 600L);
        scheduler.runTaskAsynchronously(_miniPlugin._javaPlugin, () -> {
            final JedisPooled jedis = _pluginDatabase.getJedisPooled();

            if (ServerQueries.getServerGroup(jedis, "EVENT") != null) {
                player.sendMessage(F.fMain(this) + F.fError("An event server group already exists."));
                _pending.remove(player);
                return;
            }

            ServerGroupData serverGroupData = new ServerGroupData("EVENT", "EVENT", null, 20000, 20001, 1, 0, "Arcade.jar", "Lobby_Arcade.zip", 512);
            serverGroupData.update(jedis);

            scheduler.runTask(_miniPlugin._javaPlugin, () -> {
                player.sendMessage(F.fMain(this) + F.fItem(serverGroupData._prefix + "-1") + " is being created...\n" + F.fMain() + "You will be teleported in around " + F.fItem("20 Seconds") + ".");
                _pending.remove(player);
            });

            new BukkitRunnable() {
                @Override
                public void run() {
                    final ServerData[] eventServers = ServerQueries.getServers(jedis, serverGroupData);
                    if (eventServers.length == 0) {
                        return;
                    }

                    final String serverName = eventServers[0]._name;
                    _pluginPortal.teleport(player.getName(), serverName, serverName);
                    cancel();
                }
            }.runTaskTimerAsynchronously(_miniPlugin._javaPlugin, 0L, 20L);
        });
    }

}
