package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class PluginPortal extends MiniPlugin implements PluginMessageListener {

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

    final String PROXY_CHANNEL = "BungeeCord";
    final String TELEPORT_CHANNEL = "PortalTeleport";
    final String RESTART_CHANNEL = "PortalRestart";

    PluginCommand _pluginCommand;
    PluginDatabase _pluginDatabase;

    public UUID _serverUuid;
    public String _serverName;
    public UUID _serverGroup;

    public String _serverIp;
    public String _serverPort;
    public String _serverJar;
    public String _serverWebsite;

    private Messenger _messenger;
    private final Map<String, Map<UUID, ByteArrayDataInputRunnable>> _callbacks;
    private BukkitRunnable _lastUpdateTask;

    public PluginPortal(JavaPlugin javaPlugin) {
        super(javaPlugin, "Portal");

        _serverUuid = UUID.randomUUID();
        _callbacks = new HashMap<>();
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SERVER);

        PermissionGroup.MVP._permissions.add(PERM.COMMAND_HOSTSERVER);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RESTART);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RESTART_GROUP);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_RESTART_SERVER);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_SEND);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_MOTD);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_MOTD_VIEW);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_MOTD_SET);

        try {
            _serverUuid = UUID.fromString(read(new File("_uuid.dat")));
            _serverIp = read(new File("_ip.dat"));
            _serverPort = read(new File("_port.dat"));
            _serverJar = read(new File("_jar.dat"));
            _serverWebsite = read(new File("_website.dat"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        Map<String, String> serverData = _pluginDatabase.getJedisPooled().hgetAll(ServerQueries.SERVER(_serverUuid));
        _serverName = serverData.get("name");
        _serverGroup = UUID.fromString(serverData.get("group"));

        _messenger = _javaPlugin.getServer().getMessenger();
        _messenger.registerOutgoingPluginChannel(_javaPlugin, PROXY_CHANNEL);
        _messenger.registerIncomingPluginChannel(_javaPlugin, PROXY_CHANNEL, this);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandServer(this));
        _pluginCommand.register(new CommandSend(this));
        _pluginCommand.register(new CommandRestart(this));
        _pluginCommand.register(new CommandMotd(this, _pluginDatabase));
        _pluginCommand.register(new CommandHostEvent(this, _pluginDatabase));
        _pluginCommand.register(new CommandHostServer(this, _pluginDatabase));

        _pluginDatabase.registerCallback(TELEPORT_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                String[] args = getMessage().split(",");
                String playerName = args[0];
                String serverName = args[1];
                String senderName = args.length > 2 ? args[2] : null;

                if (senderName != null) {
                    _javaPlugin.getServer().getOnlinePlayers().forEach(player1 -> {
                        if (!player1.hasPermission(PermissionGroup.ADMINISTRATOR.name())) {
                            return;
                        }
                        player1.sendMessage(F.fStaff() + F.fMain(this) + F.fItem(senderName) + " sent " + F.fItem(playerName) + " to " + F.fItem(serverName));
                    });
                }

                Player player = _javaPlugin.getServer().getPlayer(playerName);
                if (player == null) {
                    return;
                }

                if (args.length > 2) {
                    player.sendMessage(F.fMain(this) + F.fItem(senderName) + " sent you from " + F.fItem(_serverName) + " to " + F.fItem(serverName) + ".");
                } else {
                    player.sendMessage(F.fMain(this) + "You were sent from " + F.fItem(_serverName) + " to " + F.fItem(serverName) + ".");
                }

                //noinspection UnstableApiUsage
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(serverName);
                player.sendPluginMessage(_javaPlugin, PROXY_CHANNEL, out.toByteArray());
            }

        });

        _pluginDatabase.registerCallback(RESTART_CHANNEL, new MessagedRunnable(this) {

            @Override
            public void run() {
                String[] args = getMessage().split(",");
                String restartType = args[0];

                if (restartType.equals("server")) {
                    String serverName = args[1];
                    if (!_serverName.equals(serverName)) {
                        return;
                    }
                } else if (restartType.equals("group")) {
                    String groupName = args[1];
                    if (!_serverName.split("-")[0].equals(groupName)) {
                        return;
                    }
                }

                Server server = _javaPlugin.getServer();
                server.broadcastMessage(F.fMain(this) + "The server you are currently connected to is restarting. Sending you to a lobby.");
                server.getScheduler().runTaskLater(_javaPlugin, () -> server.getOnlinePlayers().forEach(player -> teleport(player.getName(), "Lobby")), 80);
                server.getScheduler().runTaskLater(_javaPlugin, server.spigot()::restart, 160);
            }

        });

        _lastUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                JedisPooled jedis = _pluginDatabase.getJedisPooled();
                BukkitScheduler scheduler = _javaPlugin.getServer().getScheduler();
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.hset(ServerQueries.SERVER(_serverUuid), "lastUpdate", Long.toString(System.currentTimeMillis())));
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.hset(ServerQueries.SERVER(_serverUuid), "serverIp", _serverIp));
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.hset(ServerQueries.SERVER(_serverUuid), "serverPort", _serverPort));
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.hset(ServerQueries.SERVER(_serverUuid), "playerCount", Integer.toString(_javaPlugin.getServer().getOnlinePlayers().size())));
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.sadd(ServerQueries.SERVERS_ACTIVE(), _serverUuid.toString()));
                scheduler.runTaskAsynchronously(_javaPlugin, () -> jedis.sadd(ServerQueries.SERVERS_HISTORY(), _serverUuid.toString()));
            }
        };
        _lastUpdateTask.runTaskTimerAsynchronously(_javaPlugin, 0, 20);
    }

    @Override
    public void onDisable() {
        _messenger.unregisterOutgoingPluginChannel(_javaPlugin, PROXY_CHANNEL);
        _messenger.unregisterIncomingPluginChannel(_javaPlugin, PROXY_CHANNEL);

        _callbacks.clear();
        _lastUpdateTask.cancel();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        //noinspection UnstableApiUsage
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (channel.equals(PROXY_CHANNEL)) {
            String subChannel = in.readUTF();
            if (!_callbacks.containsKey(subChannel)) {
                return;
            }
            _callbacks.get(subChannel).forEach((uuid, callback) -> {
                callback.setIn(in);
                callback.run();
            });
        }
    }

    public void teleport(String player, String server, String... sender) {
        if (sender.length > 0) {
            _pluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, player + "," + server + "," + sender[0]);
            return;
        }
        _pluginDatabase.getJedisPooled().publish(TELEPORT_CHANNEL, player + "," + server);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isServerActive(String name) {
        for (UUID uuid : _pluginDatabase.getJedisPooled().smembers(ServerQueries.SERVERS_ACTIVE()).stream().map(UUID::fromString).toList()) {
            ServerData serverData = new ServerData(_pluginDatabase.getJedisPooled().hgetAll(ServerQueries.SERVER(uuid)));
            if (!serverData._name.equals(name)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public void restartServer(String server) {
        _pluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, "server," + server);
    }

    public void restartGroup(String group) {
        _pluginDatabase.getJedisPooled().publish(RESTART_CHANNEL, "group," + group);
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
        _javaPlugin.getServer().sendPluginMessage(_javaPlugin, PROXY_CHANNEL, outServer.toByteArray());

        return 0;
    }

    @SuppressWarnings("unused")
    public UUID registerCallback(String channelName, ByteArrayDataInputRunnable callback) {
        UUID id = UUID.randomUUID();
        if (!_callbacks.containsKey(channelName)) {
            _callbacks.put(channelName, new HashMap<>());
        }
        _callbacks.get(channelName).put(id, callback);
        return id;
    }

    @SuppressWarnings("unused")
    public void unregisterCallback(UUID id) {
        _callbacks.forEach((s, uuidRunnableMap) -> {
            if (!uuidRunnableMap.containsKey(id)) {
                return;
            }
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
        _javaPlugin.getServer().sendPluginMessage(_javaPlugin, PROXY_CHANNEL, outServer.toByteArray());
    }

}
