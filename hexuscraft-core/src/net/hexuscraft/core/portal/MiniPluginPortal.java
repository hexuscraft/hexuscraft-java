package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.messages.PortalRestartServerGroupMessage;
import net.hexuscraft.common.messages.PortalRestartServerMessage;
import net.hexuscraft.common.messages.PortalTeleportMessage;
import net.hexuscraft.common.messages.PortalTeleportOtherMessage;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.command.*;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public final class MiniPluginPortal extends MiniPlugin<HexusPlugin> implements PluginMessageListener {

    public final Set<CommandSender> _networkChannelSpies;
    public final long _createdMillis;
    public final String _serverName;
    public final String _serverGroup;
    public final Map<ServerGroupData, ServerData[]> _serverCache;
    private final String PROXY_CHANNEL = "BungeeCord";
    private final Messenger _messenger;
    private final Map<String, Map<UUID, ByteArrayDataInputRunnable>> _callbacks;
    private MiniPluginCommand _pluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private BukkitTask _updateServerDataTask;
    private BukkitTask _updateServerCacheTask;

    public MiniPluginPortal(final HexusPlugin plugin) {
        super(plugin, "Portal");

        _createdMillis = System.currentTimeMillis();
        _callbacks = new HashMap<>();
        _serverCache = new HashMap<>();
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
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);

        PermissionGroup.MEMBER._permissions.addAll(
                List.of(PERM.COMMAND_SERVER, PERM.COMMAND_PERFORMANCE, PERM.COMMAND_MOTD, PERM.COMMAND_MOTD_VIEW));
        PermissionGroup.MVP._permissions.add(PERM.COMMAND_HOSTSERVER);
        PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOSTEVENT);
        PermissionGroup.ADMINISTRATOR._permissions.addAll(
                List.of(PERM.COMMAND_SEND, PERM.COMMAND_MOTD_SET, PERM.COMMAND_NETWORK, PERM.COMMAND_NETWORK_SPY,
                        PERM.COMMAND_NETWORK_GROUP, PERM.COMMAND_NETWORK_GROUP_CREATE,
                        PERM.COMMAND_NETWORK_GROUP_DELETE, PERM.COMMAND_NETWORK_GROUP_LIST, PERM.COMMAND_NETWORK_SERVER,
                        PERM.COMMAND_NETWORK_GROUP_RESTART, PERM.COMMAND_NETWORK_SERVER_RESTART));
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandServer(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandSend(this));
        _pluginCommand.register(new CommandHostEvent(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandHostServer(this, _miniPluginDatabase));
        _pluginCommand.register(new CommandNetwork(this, _miniPluginDatabase));

        _miniPluginDatabase.registerConsumer("*", (_, channelName, message) -> _networkChannelSpies.forEach(
                commandSender -> commandSender.sendMessage(F.fSub(channelName, message))));

        _miniPluginDatabase.registerConsumer(PortalTeleportMessage.CHANNEL_NAME,
                (_, _, rawMessage) -> _hexusPlugin.runAsync(() -> {
                    final PortalTeleportMessage message = PortalTeleportMessage.fromString(rawMessage);

                    final AtomicReference<String> targetName = new AtomicReference<>();

                    _hexusPlugin.getServer().getOnlinePlayers().forEach(targetPlayer -> {
                        if (!targetPlayer.getUniqueId().equals(message._uniqueId())) return;
                        targetName.set(targetPlayer.getName());

                        targetPlayer.sendMessage(F.fMain(this, "You were sent from ", F.fItem(_serverName), " to ",
                                F.fItem(message._serverName()), "."));

                        // TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
                        //noinspection UnstableApiUsage
                        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(message._serverName());
                        targetPlayer.sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, out.toByteArray());
                    });
                }));

        _miniPluginDatabase.registerConsumer(PortalTeleportOtherMessage.CHANNEL_NAME,
                (_, _, rawMessage) -> _hexusPlugin.runAsync(() -> {
                    final PortalTeleportOtherMessage message = PortalTeleportOtherMessage.fromString(rawMessage);

                    final AtomicReference<String> targetName = new AtomicReference<>();
                    final AtomicReference<String> senderName = new AtomicReference<>();

                    _hexusPlugin.getServer().getOnlinePlayers().forEach(targetPlayer -> {
                        if (targetPlayer.getUniqueId().equals(message._targetUniqueId())) {
                            targetName.set(targetPlayer.getName());

                            targetPlayer.sendMessage(F.fMain(this, "You were sent from ", F.fItem(_serverName), " to ",
                                    F.fItem(message._serverName()), "."));

                            // TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
                            //noinspection UnstableApiUsage
                            final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("Connect");
                            out.writeUTF(message._serverName());
                            targetPlayer.sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, out.toByteArray());
                        }
                        if (targetPlayer.getUniqueId().equals(message._senderUniqueId()))
                            senderName.set(targetPlayer.getName());
                    });

                    if (message._senderUniqueId().equals(UtilUniqueId.EMPTY_UUID)) return;

                    if (targetName.get() == null) try {
                        targetName.set(PlayerSearch.offlinePlayerSearch(message._targetUniqueId()).getName());
                    } catch (final IOException ex) {
                        logWarning("IOException while fetching unique id of portal teleport target '" +
                                message._targetUniqueId() + "': " + ex.getMessage());
                        return;
                    }

                    if (senderName.get() == null) try {
                        senderName.set(PlayerSearch.offlinePlayerSearch(message._senderUniqueId()).getName());
                    } catch (final IOException ex) {
                        logWarning("IOException while fetching unique id of portal teleport sender '" +
                                message._senderUniqueId() + "': " + ex.getMessage());
                        return;
                    }

                    _hexusPlugin.getServer().getOnlinePlayers().stream()
                            .filter(player -> player.hasPermission(PermissionGroup.TRAINEE.name())).forEach(
                                    player -> player.sendMessage(F.fStaff() +
                                            F.fMain(this, F.fItem(senderName), " sent ", F.fItem(targetName), " to ",
                                                    F.fItem(message._serverName()))));
                }));

        _miniPluginDatabase.registerConsumer(PortalRestartServerMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
            final PortalRestartServerMessage message = PortalRestartServerMessage.fromString(rawMessage);
            if (!message._serverName().equals(_serverName)) return;
            _hexusPlugin.getServer().shutdown();
        });

        _miniPluginDatabase.registerConsumer(PortalRestartServerGroupMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
            final PortalRestartServerGroupMessage message = PortalRestartServerGroupMessage.fromString(rawMessage);
            if (!message._groupName().equals(_serverGroup)) return;
            _hexusPlugin.getServer().shutdown();
        });

        final Server server = _hexusPlugin.getServer();

        _updateServerDataTask = _hexusPlugin.runAsyncTimer(() -> {
            final ServerListPingEvent ping =
                    new ServerListPingEvent(new InetSocketAddress(server.getIp(), server.getPort()).getAddress(),
                            server.getMotd(), server.getOnlinePlayers().size(), server.getMaxPlayers());
            server.getPluginManager().callEvent(ping);

            final OptionalDouble averageTps = Arrays.stream(MinecraftServer.getServer().recentTps).average();

            new ServerData(_serverName, server.getIp(), server.getMaxPlayers(), _createdMillis, _serverGroup,
                    ping.getMotd(), ping.getNumPlayers(), server.getPort(), averageTps.orElse(2D),
                    System.currentTimeMillis(), false).update(_miniPluginDatabase.getUnifiedJedis());
        }, 0, 20);

        _updateServerCacheTask = _hexusPlugin.runAsyncTimer(() -> {
            try {
                final UnifiedJedis jedis = _miniPluginDatabase.getUnifiedJedis();
                final Map<ServerGroupData, ServerData[]> cache = new HashMap<>();
                Arrays.stream(ServerQueries.getServerGroups(jedis))
                        .forEach(serverGroupData -> cache.put(serverGroupData, serverGroupData.getServers(jedis)));
                _serverCache.clear();
                _serverCache.putAll(cache);
            } catch (final JedisException ex) {
                logSevere(ex);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        _messenger.unregisterOutgoingPluginChannel(_hexusPlugin, PROXY_CHANNEL);
        _messenger.unregisterIncomingPluginChannel(_hexusPlugin, PROXY_CHANNEL);

        _callbacks.clear();

        if (_updateServerDataTask != null) _updateServerDataTask.cancel();

        final Server server = _hexusPlugin.getServer();

        final ServerListPingEvent ping =
                new ServerListPingEvent(new InetSocketAddress(server.getIp(), server.getPort()).getAddress(),
                        server.getMotd(), server.getOnlinePlayers().size(), server.getMaxPlayers());

        server.getPluginManager().callEvent(ping);

        new ServerData(_serverName, server.getIp(), ping.getMaxPlayers(), _createdMillis, _serverGroup, "DEAD",
                ping.getNumPlayers(), server.getPort(), 20, System.currentTimeMillis(), false).update(
                _miniPluginDatabase.getUnifiedJedis());
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
        // TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
        //noinspection UnstableApiUsage
        final ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (channel.equals(PROXY_CHANNEL)) {
            final String subChannel = in.readUTF();
            if (!_callbacks.containsKey(subChannel)) return;
            _callbacks.get(subChannel).forEach((_, callback) -> {
                callback.setIn(in);
                callback.run();
            });
        }
    }

    public BukkitTask teleportAsync(final UUID uniqueId, final String serverName) {
        return _hexusPlugin.runAsync(() -> _miniPluginDatabase.getUnifiedJedis()
                .publish(PortalTeleportMessage.CHANNEL_NAME,
                        new PortalTeleportMessage(uniqueId, serverName).toString()));
    }

    public BukkitTask teleportAsync(final UUID targetUniqueId, final String serverName, final UUID senderUniqueId) {
        return _hexusPlugin.runAsync(() -> _miniPluginDatabase.getUnifiedJedis()
                .publish(PortalTeleportOtherMessage.CHANNEL_NAME,
                        new PortalTeleportOtherMessage(targetUniqueId, serverName, senderUniqueId).toString()));
    }

    public BukkitTask teleportAsync(final OfflinePlayer target, final String serverName) {
        return teleportAsync(target.getUniqueId(), serverName);
    }

    public BukkitTask teleportAsync(final OfflinePlayer target, final String serverName, final OfflinePlayer sender) {
        return teleportAsync(target.getUniqueId(), serverName, sender.getUniqueId());
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask teleportPlayerToRandomServer(final Player player, final String serverGroupName) {
        player.sendMessage(F.fMain(this, "Locating a ", F.fItem(serverGroupName), " server... ", C.fMagic + "..."));
        return _hexusPlugin.runAsync(() -> {
            final ServerData[] availableServers =
                    Arrays.stream(ServerQueries.getServers(_miniPluginDatabase.getUnifiedJedis(), serverGroupName))
                            .filter(serverData -> !serverData._name.equals(_serverName)).toArray(ServerData[]::new);
            if (availableServers.length == 0) _hexusPlugin.runSync(() -> player.sendMessage(F.fMain(this,
                    F.fError("Sorry, we were unable to locate a ", F.fItem(serverGroupName), " server."))));

            _hexusPlugin.runSync(
                    () -> teleportAsync(player, availableServers[new Random().nextInt(availableServers.length)]._name));
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public ServerData getServerDataFromName(final String serverName) {
        return ServerQueries.getServer(_miniPluginDatabase.getUnifiedJedis(), serverName);
    }

    public ServerGroupData getServerGroupDataFromName(final String serverGroupName) {
        return ServerQueries.getServerGroup(_miniPluginDatabase.getUnifiedJedis(), serverGroupName);
    }

    public void restartServerYields(final String server) {
        _miniPluginDatabase.getUnifiedJedis().publish(PortalRestartServerGroupMessage.CHANNEL_NAME,
                new PortalRestartServerMessage(server).toString());
    }

    public void restartServerGroupYields(final String group) {
        _miniPluginDatabase.getUnifiedJedis().publish(PortalRestartServerGroupMessage.CHANNEL_NAME,
                new PortalRestartServerGroupMessage(group).toString());
    }

    public String read(File file) throws FileNotFoundException {
        return new Scanner(file).nextLine();
    }

    @SuppressWarnings({"SameReturnValue", "unused"})
    public int getPlayerCount(String name) {
        // TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
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
        // TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
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

    public enum PERM implements IPermission {
        COMMAND_SEND, COMMAND_SERVER, COMMAND_MOTD, COMMAND_MOTD_VIEW, COMMAND_MOTD_SET, COMMAND_HOSTSERVER, COMMAND_HOSTEVENT, COMMAND_PERFORMANCE,

        COMMAND_NETWORK, COMMAND_NETWORK_GROUP, COMMAND_NETWORK_GROUP_CREATE, COMMAND_NETWORK_GROUP_DELETE, COMMAND_NETWORK_GROUP_LIST, COMMAND_NETWORK_GROUP_RESTART,

        COMMAND_NETWORK_SERVER, COMMAND_NETWORK_SERVER_RESTART, COMMAND_NETWORK_SERVER_LIST,

        COMMAND_NETWORK_SPY,

        COMMAND_NETWORK_DATABASE, COMMAND_NETWORK_DATABASE_SPY
    }

}
