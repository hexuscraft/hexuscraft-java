package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.command.*;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MiniPluginPortal extends MiniPlugin<HexusPlugin> implements PluginMessageListener {

    public enum PERM implements IPermission {
        COMMAND_SEND, COMMAND_SERVER, COMMAND_MOTD, COMMAND_MOTD_VIEW, COMMAND_MOTD_SET, COMMAND_HOSTSERVER, COMMAND_HOSTEVENT, COMMAND_PERFORMANCE,

        COMMAND_NETWORK, COMMAND_NETWORK_GROUP, COMMAND_NETWORK_GROUP_CREATE, COMMAND_NETWORK_GROUP_DELETE, COMMAND_NETWORK_GROUP_LIST, COMMAND_NETWORK_GROUP_RESTART,

        COMMAND_NETWORK_SERVER, COMMAND_NETWORK_SERVER_RESTART, COMMAND_NETWORK_SERVER_LIST,

        COMMAND_NETWORK_SPY,

        COMMAND_NETWORK_DATABASE, COMMAND_NETWORK_DATABASE_SPY
    }

    // TODO: These database channels need refactoring to hexuscraft-database at some point
    private final String PROXY_CHANNEL = "BungeeCord";
    private final String TELEPORT_CHANNEL = "PortalTeleport";
    private final String RESTART_CHANNEL = "PortalRestart";
    @SuppressWarnings("FieldCanBeLocal")

    private MiniPluginCommand _pluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;

    public final Set<CommandSender> _networkChannelSpies;

    public final long _created;
    public final String _serverName;
    public final String _serverGroup;

    private final Messenger _messenger;
    private final Map<String, Map<UUID, ByteArrayDataInputRunnable>> _callbacks;
    private BukkitTask _updateTask;

    private final AtomicBoolean _isServerRestarting = new AtomicBoolean(false);

    public MiniPluginPortal(final HexusPlugin plugin) {
        super(plugin, "Portal");

        _created = System.currentTimeMillis();
        _callbacks = new HashMap<>();
        _networkChannelSpies = new HashSet<>();

        try {
            _serverName = read(new File("_name.dat"));
            _serverGroup = read(new File("_group.dat"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        _messenger = _hexusPlugin.getServer().getMessenger();
        _messenger.registerOutgoingPluginChannel(_hexusPlugin, PROXY_CHANNEL);
        _messenger.registerIncomingPluginChannel(_hexusPlugin, PROXY_CHANNEL, this);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);

        PermissionGroup.MEMBER._permissions.addAll(List.of(PERM.COMMAND_SERVER, PERM.COMMAND_PERFORMANCE, PERM.COMMAND_MOTD, PERM.COMMAND_MOTD_VIEW));
        PermissionGroup.MVP._permissions.add(PERM.COMMAND_HOSTSERVER);
        PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOSTEVENT);
        PermissionGroup.ADMINISTRATOR._permissions.addAll(List.of(PERM.COMMAND_SEND, PERM.COMMAND_MOTD_SET, PERM.COMMAND_NETWORK, PERM.COMMAND_NETWORK_SPY, PERM.COMMAND_NETWORK_GROUP, PERM.COMMAND_NETWORK_GROUP_CREATE, PERM.COMMAND_NETWORK_GROUP_DELETE, PERM.COMMAND_NETWORK_GROUP_LIST, PERM.COMMAND_NETWORK_SERVER, PERM.COMMAND_NETWORK_GROUP_RESTART, PERM.COMMAND_NETWORK_SERVER_RESTART));
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandServer(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandSend(this));
        _pluginCommand.register(new CommandHostEvent(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandHostServer(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandNetwork(this, _miniPluginDatabase));

        _miniPluginDatabase.registerCallback("*", new MessagedRunnable(this) {
            @Override
            public void run() {
                _networkChannelSpies.forEach(commandSender -> commandSender.sendMessage(F.fStaff() + F.fSub(this, getChannelName() + " - " + getMessage())));
            }
        });
        _miniPluginDatabase.registerCallback(TELEPORT_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                final String[] args = getMessage().split(",");
                final String playerName = args[0];
                final String serverName = args[1];
                final String senderName = args.length > 2 ? args[2] : null;

                if (senderName != null) {
                    _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player1 -> {
                        if (!player1.hasPermission(PermissionGroup.ADMINISTRATOR.name())) return;
                        player1.sendMessage(F.fStaff() + F.fMain(this, F.fItem(senderName), " sent ", F.fItem(playerName), " to ", F.fItem(serverName)));
                    });
                }

                final Player player = _miniPlugin._hexusPlugin.getServer().getPlayer(playerName);
                if (player == null) return;

                if (args.length > 2) {
                    player.sendMessage(F.fMain(this, F.fItem(senderName), " sent you from ", F.fItem(_serverName), " to ", F.fItem(serverName), "."));
                } else {
                    player.sendMessage(F.fMain(this, "You were sent from ", F.fItem(_serverName), " to ", F.fItem(serverName), "."));
                }

                //noinspection UnstableApiUsage
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(serverName);
                player.sendPluginMessage(_miniPlugin._hexusPlugin, PROXY_CHANNEL, out.toByteArray());
            }

        });

        _miniPluginDatabase.registerCallback(RESTART_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                final String[] args = getMessage().split(",");
                final String restartType = args[0];

                if (restartType.equals("server")) {
                    if (!_serverName.equals(args[1])) return;
                } else if (restartType.equals("group")) {
                    if (!_serverGroup.equals(args[1])) return;
                } else {
                    logInfo("Received unknown restart type: " + restartType);
                    return;
                }

                final Server server = _miniPlugin._hexusPlugin.getServer();
                _isServerRestarting.set(true);
                server.broadcastMessage(F.fMain(this, "The server you are currently connected to is restarting. Sending you to a lobby."));

                _miniPlugin._hexusPlugin.runAsyncLater(() -> _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> teleportPlayerToRandomServer(player, "Lobby")), 80);
                _miniPlugin._hexusPlugin.runSyncLater(server::shutdown, 160);
            }

        });

        final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();
        final Server server = _hexusPlugin.getServer();
        final OptionalDouble averageTps = Arrays.stream(MinecraftServer.getServer().recentTps).average();

        final ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress("127.0.0.1", 0).getAddress(), "", 0, 0);
        server.getPluginManager().callEvent(ping);

        _updateTask = server.getScheduler().runTaskTimerAsynchronously(_hexusPlugin, () -> new ServerData(_serverName, server.getIp(), server.getMaxPlayers(), _created, _serverGroup, ping.getMotd(), ping.getNumPlayers(), server.getPort(), averageTps.orElse(2D), System.currentTimeMillis()).update(jedis), 0, 20);
    }

    @Override
    public void onDisable() {
        _messenger.unregisterOutgoingPluginChannel(_hexusPlugin, PROXY_CHANNEL);
        _messenger.unregisterIncomingPluginChannel(_hexusPlugin, PROXY_CHANNEL);

        _callbacks.clear();

        if (_updateTask != null) _updateTask.cancel();

        final JedisPooled jedis = _miniPluginDatabase.getJedisPooled();
        final Server server = _hexusPlugin.getServer();

        final ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress(server.getIp(), server.getPort()).getAddress(), server.getMotd(), server.getOnlinePlayers().size(), server.getMaxPlayers());
        server.getPluginManager().callEvent(ping);

        new ServerData(_serverName, server.getIp(), ping.getMaxPlayers(), _created, _serverGroup, ping.getMotd(), ping.getNumPlayers(), server.getPort(), 0, Integer.MAX_VALUE).update(jedis);
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        if (!_isServerRestarting.get()) return;
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, C.cRed + C.fBold + "This server is restarting" + C.fReset + "\nThe server you are attempting to join is currently restarting.\nPlease try again later.");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
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

    public void teleport(final String player, final String server, final String sender) {
        if (sender != null) {
            _hexusPlugin.runAsync(() -> _miniPluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, String.join(",", player, server, sender)));
            return;
        }
        _hexusPlugin.runAsync(() -> _miniPluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, String.join(",", player, server)));
    }

    public void teleport(final String player, final String server) {
        teleport(player, server, null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask teleportPlayerToRandomServer(final Player player, final String serverGroupName) {
        player.sendMessage(F.fMain(this, "Locating a ", F.fItem(serverGroupName), " server... ", C.fMagic + "..."));
        return _hexusPlugin.runAsync(() -> {
            final ServerData[] availableServers = Arrays.stream(ServerQueries.getServers(_miniPluginDatabase.getJedisPooled(), serverGroupName)).filter(serverData -> !serverData._name.equals(_serverName)).toArray(ServerData[]::new);
            if (availableServers.length == 0)
                _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this, F.fError("Sorry, we were unable to locate a ", F.fItem(serverGroupName), " server."))));

            _hexusPlugin.runSync(() -> teleport(player.getName(), availableServers[new Random().nextInt(availableServers.length)]._name));
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public ServerData getServerDataFromName(final String serverName) {
        return ServerQueries.getServer(_miniPluginDatabase.getJedisPooled(), serverName);
    }

    public ServerGroupData getServerGroupDataFromName(final String serverGroupName) {
        return ServerQueries.getServerGroup(_miniPluginDatabase.getJedisPooled(), serverGroupName);
    }

    public void restartServer(String server) {
        _miniPluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, String.join(",", "server", server));
    }

    public void restartGroup(String group) {
        _miniPluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, String.join(",", "group", group));
    }

    public String read(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine();
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public int getPlayerCount(String name) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput outServer = ByteStreams.newDataOutput();
        outServer.writeUTF("PlayerCount");
        outServer.writeUTF(name);
        _hexusPlugin.getServer().sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, outServer.toByteArray());

        return 0;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public UUID registerCallback(final String channelName, final ByteArrayDataInputRunnable callback) {
        final UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public void unregisterCallback(UUID id) {
        _callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) return;
            uuidRunnableMap.remove(id);
        });
    }

    @SuppressWarnings("unused")
    public void sendProxyMessage(String... data) {
        //noinspection UnstableApiUsage
        ByteArrayDataOutput outServer = ByteStreams.newDataOutput();
        for (String s : data) {
            outServer.writeUTF(s);
        }
        _hexusPlugin.getServer().sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, outServer.toByteArray());
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        _networkChannelSpies.remove(event.getPlayer());
    }

}
