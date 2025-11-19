package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class CommandHostServer extends BaseCommand<MiniPluginPortal> {

    private final MiniPluginDatabase _miniPluginDatabase;

    private final Set<CommandSender> _pending;

    public CommandHostServer(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "hostserver", "", "Start a new private server.", Set.of("hps"), MiniPluginPortal.PERM.COMMAND_HOSTSERVER);

        _miniPluginDatabase = miniPluginDatabase;
        _pending = new HashSet<>();
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        if (_pending.contains(sender)) {
            sender.sendMessage(F.fMain(this, F.fError("You already have a pending request! Please wait until your previous request has completed...")));
            return;
        }
        _pending.add(sender);

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();

            sender.sendMessage(F.fMain(this, "Checking for existing server data..."));

            final String serverName;
            // TODO: Async this
            try {
                serverName = ((Callable<String>) () -> {
                    for (ServerData serverData : ServerQueries.getServers(jedis))
                        if (serverData._group.split("-", 2)[0].equals(sender.getName())) return serverData._name;
                    return null;
                }).call();
            } catch (Exception e) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("There was an error fetching existing server data. Maybe try again later?")));
                return;
            }

            if (serverName != null) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("You are already the host of a private server!\n"), F.fMain("", "Connect to it with ", F.fItem("/server " + serverName + "."))));
                return;
            }

            try {
                sender.sendMessage(F.fMain(this, "Creating server group..."));
                final int port = ThreadLocalRandom.current().nextInt(50000, 51000);
                final ServerGroupData groupData = new ServerGroupData(sender.getName(), PermissionGroup.MEMBER.name(), port - 1, port, 1, 0, "Arcade.jar", "Arcade.zip", 512, 40, false, 10000, new String[]{});
                groupData.update(jedis);
                sender.sendMessage(F.fMain(this, F.fSuccess("Successfully created server group."), "\n", F.fMain("", "You will be automatically teleported to your server soon.")));
            } catch (Exception e) {
                _pending.remove(sender);
                sender.sendMessage(F.fMain(this, F.fError("There was an error performing your request. Maybe try again later?")));
                return;
            }

            final AtomicReference<BukkitTask> bukkitTaskAtomicReference = new AtomicReference<>();
            final long start = System.currentTimeMillis();

            final AtomicBoolean sent10sReminder = new AtomicBoolean(false);
            final AtomicBoolean sent20sReminder = new AtomicBoolean(false);
            final AtomicBoolean sent30sReminder = new AtomicBoolean(false);
            final AtomicBoolean sent40sReminder = new AtomicBoolean(false);
            final AtomicBoolean sent50sReminder = new AtomicBoolean(false);

            bukkitTaskAtomicReference.set(_miniPlugin._hexusPlugin.runAsyncTimer(() -> {
                if (!sent10sReminder.get() && System.currentTimeMillis() - start > 10000) {
                    sent10sReminder.set(true);
                    sender.sendMessage(F.fMain(this, "Still waiting for your server to start... (10s elapsed)"));
                    return;
                }

                if (!sent20sReminder.get() && System.currentTimeMillis() - start > 20000) {
                    sent20sReminder.set(true);
                    sender.sendMessage(F.fMain(this, "Still waiting for your server to start... (20s elapsed)"));
                    return;
                }

                if (!sent30sReminder.get() && System.currentTimeMillis() - start > 30000) {
                    sent30sReminder.set(true);
                    sender.sendMessage(F.fMain(this, "Still waiting for your server to start... (30s elapsed)"));
                    return;
                }

                if (!sent40sReminder.get() && System.currentTimeMillis() - start > 40000) {
                    sent40sReminder.set(true);
                    sender.sendMessage(F.fMain(this, "Still waiting for your server to start... (40s elapsed)"));
                    return;
                }

                if (!sent50sReminder.get() && System.currentTimeMillis() - start > 50000) {
                    sent50sReminder.set(true);
                    sender.sendMessage(F.fMain(this, "Still waiting for your server to start... (50s elapsed)"));
                    return;
                }

                if (System.currentTimeMillis() - start > 60000) {
                    sender.sendMessage(F.fMain(this, F.fError("Could not locate your server within 60 seconds. There might not be enough resources available to start your server. Maybe try again later?")));
                    _pending.remove(sender);
                    bukkitTaskAtomicReference.get().cancel();
                    return;
                }

                for (ServerData serverData : ServerQueries.getServers(jedis, new ServerGroupData(sender.getName(), Map.of()))) {
                    _miniPlugin._hexusPlugin.runAsyncLater(() -> _miniPlugin.teleport(sender.getName(), serverData._name), 20L);
                    _pending.remove(sender);
                    bukkitTaskAtomicReference.get().cancel();
                    return;
                }
            }, 100L, 20L));
        });
    }
}
