package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.command.*;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.*;

public class PluginPortal extends MiniPlugin<HexusPlugin> implements PluginMessageListener {

    public enum PERM implements IPermission {
        COMMAND_RESTART,
        COMMAND_RESTART_GROUP,
        COMMAND_RESTART_SERVER,
        COMMAND_SEND,
        COMMAND_SERVER,
        COMMAND_MOTD,
        COMMAND_MOTD_VIEW,
        COMMAND_MOTD_SET,
        COMMAND_HOSTSERVER,
        COMMAND_HOSTEVENT
    }

    private final String PROXY_CHANNEL = "BungeeCord";
    private final String TELEPORT_CHANNEL = "PortalTeleport";
    private final String RESTART_CHANNEL = "PortalRestart";

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;

    public final long _created;
    public final String _serverName;
    public final String _serverGroup;

    private final Messenger _messenger;
    private final Map<String, Map<UUID, ByteArrayDataInputRunnable>> _callbacks;
    private BukkitTask _updateTask;

    public PluginPortal(final HexusPlugin plugin) {
        super(plugin, "Portal");

        _created = System.currentTimeMillis();
        _callbacks = new HashMap<>();

        try {
            _serverName = read(new File("_name.dat"));
            _serverGroup = read(new File("_group.dat"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        _messenger = _plugin.getServer().getMessenger();
        _messenger.registerOutgoingPluginChannel(_plugin, PROXY_CHANNEL);
        _messenger.registerIncomingPluginChannel(_plugin, PROXY_CHANNEL, this);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SERVER);
        PermissionGroup.MVP._permissions.add(PERM.COMMAND_HOSTSERVER);
        PermissionGroup.ADMINISTRATOR._permissions.addAll(List.of(
                PERM.COMMAND_RESTART,
                PERM.COMMAND_RESTART_GROUP,
                PERM.COMMAND_RESTART_SERVER,
                PERM.COMMAND_SEND,
                PERM.COMMAND_MOTD,
                PERM.COMMAND_MOTD_VIEW,
                PERM.COMMAND_MOTD_SET
        ));
        PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOSTEVENT);

    }

    @Override
    public final void onEnable() {
        _pluginCommand.register(new CommandServer(this, _pluginDatabase));
        _pluginCommand.register(new CommandSend(this));
        _pluginCommand.register(new CommandRestart(this));
        _pluginCommand.register(new CommandMotd(this, _pluginDatabase));
        _pluginCommand.register(new CommandHostEvent(this, _pluginDatabase));
        _pluginCommand.register(new CommandHostServer(this, _pluginDatabase));

        _pluginDatabase.registerCallback(TELEPORT_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                final String[] args = getMessage().split(",");
                final String playerName = args[0];
                final String serverName = args[1];
                final String senderName = args.length > 2 ? args[2] : null;

                if (senderName != null) {
                    _plugin._plugin.getServer().getOnlinePlayers().forEach(player1 -> {
                        if (!player1.hasPermission(PermissionGroup.ADMINISTRATOR.name())) return;
                        player1.sendMessage(F.fSub(this) + F.fItem(senderName) + " sent " + F.fItem(playerName) + " to " + F.fItem(serverName));
                    });
                }

                final Player player = _plugin._plugin.getServer().getPlayer(playerName);
                if (player == null) return;

                if (args.length > 2) {
                    player.sendMessage(F.fMain(this) + F.fItem(senderName) + " sent you from " + F.fItem(_serverName) + " to " + F.fItem(serverName) + ".");
                } else {
                    player.sendMessage(F.fMain(this) + "You were sent from " + F.fItem(_serverName) + " to " + F.fItem(serverName) + ".");
                }

                //noinspection UnstableApiUsage
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(serverName);
                player.sendPluginMessage(_plugin._plugin, PROXY_CHANNEL, out.toByteArray());
            }

        });

        _pluginDatabase.registerCallback(RESTART_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                final String[] args = getMessage().split(",");
                final String restartType = args[0];

                if (restartType.equals("server")) {
                    if (!_serverName.equals(args[1])) return;
                } else if (restartType.equals("group")) {
                    if (!_serverGroup.equals(args[1])) return;
                } else {
                    log("Received unknown restart type: " + restartType);
                    return;
                }

                final Server server = _plugin._plugin.getServer();
                final BukkitScheduler scheduler = server.getScheduler();
                server.broadcastMessage(F.fMain(this) + "The server you are currently connected to is restarting. Sending you to a lobby.");
                scheduler.runTaskLater(_plugin._plugin, () -> server.getOnlinePlayers().forEach(player -> teleport(player.getName(), "Lobby")), 80);
                scheduler.runTaskLater(_plugin._plugin, server.spigot()::restart, 160);
            }

        });

        final JedisPooled jedis = _pluginDatabase.getJedisPooled();
        final Server server = _plugin.getServer();
        final OptionalDouble averageTps = Arrays.stream(MinecraftServer.getServer().recentTps).average();

        final ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress("127.0.0.1", 0).getAddress(), "", 0, 0);
        server.getPluginManager().callEvent(ping);

        _updateTask = server.getScheduler().runTaskTimerAsynchronously(_plugin, () -> new ServerData(
                _serverName,

                server.getIp(),
                server.getMaxPlayers(),
                _created,
                _serverGroup,
                ping.getMotd(),
                ping.getNumPlayers(),
                server.getPort(),
                averageTps.orElse(2D),
                System.currentTimeMillis()
        ).update(jedis), 0, 20);
    }

    @Override
    public final void onDisable() {
        _messenger.unregisterOutgoingPluginChannel(_plugin, PROXY_CHANNEL);
        _messenger.unregisterIncomingPluginChannel(_plugin, PROXY_CHANNEL);

        _callbacks.clear();

        _updateTask.cancel();
        _updateTask = null;

        final JedisPooled jedis = _pluginDatabase.getJedisPooled();
        final Server server = _plugin.getServer();

        final ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress("127.0.0.1", 0).getAddress(), "", 0, 0);
        server.getPluginManager().callEvent(ping);

        new ServerData(
                _serverName,

                server.getIp(),
                server.getMaxPlayers(),
                _created,
                _serverGroup,
                ping.getMotd(),
                ping.getNumPlayers(),
                server.getPort(),
                0,
                System.currentTimeMillis()
        ).update(jedis);
    }
    
    @Override
    public final void onPluginMessageReceived(String channel, Player player, byte[] message) {
        //noinspection UnstableApiUsage
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (channel.equals(PROXY_CHANNEL)) {
            String subChannel = in.readUTF();
            if (!_callbacks.containsKey(subChannel)) return;
            _callbacks.get(subChannel).forEach((uuid, callback) -> {
                callback.setIn(in);
                callback.run();
            });
        }
    }

    public final void teleport(final String player, final String server, final String sender) {
        if (sender != null) {
            _pluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, String.join(",", player, server, sender));
            return;
        }
        _pluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, String.join(",", player, server));
    }

    public final void teleport(final String player, final String server) {
        teleport(player, server, null);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean doesServerExistWithName(String name) {
        return ServerQueries.getServer(_pluginDatabase.getJedisPooled(), name) != null;
    }

    public final boolean doesServerGroupExist(final String name) {
        return ServerQueries.getServerGroup(_pluginDatabase.getJedisPooled(), name) != null;
    }

    public final void restartServer(String server) {
        _pluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, String.join(",", "server", server));
    }

    public final void restartGroup(String group) {
        _pluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, String.join(",", "group", group));
    }

    public final String read(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine();
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public final int getPlayerCount(String name) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput outServer = ByteStreams.newDataOutput();
        outServer.writeUTF("PlayerCount");
        outServer.writeUTF(name);
        _plugin.getServer().sendPluginMessage(_plugin, PROXY_CHANNEL, outServer.toByteArray());

        return 0;
    }

    @SuppressWarnings("unused")
    public final UUID registerCallback(String channelName, ByteArrayDataInputRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public final void unregisterCallback(UUID id) {
        _callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) return;
            uuidRunnableMap.remove(id);
        });
    }

    @SuppressWarnings("unused")
    public final void sendProxyMessage(String... data) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput outServer = ByteStreams.newDataOutput();
        for (String s : data) {
            outServer.writeUTF(s);
        }
        _plugin.getServer().sendPluginMessage(_plugin, PROXY_CHANNEL, outServer.toByteArray());
    }

}
