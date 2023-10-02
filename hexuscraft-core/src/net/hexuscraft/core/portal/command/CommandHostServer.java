package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class CommandHostServer extends BaseCommand {

    PluginDatabase _pluginDatabase;

    Set<Player> _pending;

    public CommandHostServer(PluginPortal pluginPortal, PluginDatabase pluginDatabase) {
        super(pluginPortal, "hostserver", "", "Start a new private server.", Set.of("hps"), PluginPortal.PERM.COMMAND_HOSTSERVER);

        _pending = new HashSet<>();
        _pluginDatabase = pluginDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof final Player player)) {
            sender.sendMessage("Only players can create private servers.");
            return;
        }

        if (_pending.contains(player)) {
            player.sendMessage(F.fMain(this) + F.fError("You already have a pending request! Please wait until your previous request has completed..."));
            return;
        }

        _pending.add(player);

        final BukkitScheduler scheduler = _miniPlugin._javaPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(_miniPlugin._javaPlugin, () -> {
            try {
                final String serverName = ((Callable<String>) () -> {
                    for (ServerData serverData : ServerQueries.getServers(_pluginDatabase.getJedisPooled())) {
                        if (serverData._group.split("-", 2)[0].equals(player.getName())) {
                            return serverData._name;
                        }
                    }
                    return null;
                }).call();

                scheduler.runTask(_miniPlugin._javaPlugin, () -> {
                    if (serverName == null) {
                        player.sendMessage(F.fMain(this) + F.fItem(player.getName() + "-1") + " is being created...\n" + F.fMain("") + "You will be teleported in around " + F.fItem("30 Seconds") + ".");
                        return;
                    }
                    player.sendMessage(F.fMain(this) + "You are already the host of a private server!\n" + F.fMain("") + "Connect to it with " + F.fItem("/server " + serverName + "."));
                });
            } catch (Exception e) {
                scheduler.runTask(_miniPlugin._javaPlugin, () -> player.sendMessage(F.fMain(this) + F.fError("There was an error while contacting the database. Maybe try again later?")));
            } finally {
                _pending.remove(player);
            }
        });
    }
}
