package net.hexuscraft.core.portal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.messages.PortalRestartServerGroupMessage;
import net.hexuscraft.common.database.messages.PortalRestartServerMessage;
import net.hexuscraft.common.database.messages.PortalTeleportMessage;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.actionbar.CoreActionBar;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.gui.Gui;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.command.*;
import net.hexuscraft.core.portal.gui.GuiCreateServerGroup;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.exceptions.JedisException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CorePortal extends MiniPlugin<HexusPlugin> implements PluginMessageListener {

	public static String PROXY_CHANNEL = "BungeeCord";
	public static int MIN_PORT_PRIVATE_SERVERS = 59900;
	public static int MAX_PORT_PRIVATE_SERVERS = 59999;
	public static int EVENT_SERVER_PORT = 50003;
	final Messenger _messenger;
	final Map<String, Map<UUID, ByteArrayDataInputRunnable>> _callbacks;
	final Set<ServerGroupData> _serverGroupCache;
	final Set<ServerData> _serverCache;
	public Set<CommandSender> _networkChannelSpies;
	public long _createdMillis;
	public String _serverName;
	public String _serverGroupName;
	CoreActionBar _coreActionBar;
	CorePermission _corePermission;
	CoreCommand _coreCommand;
	CoreDatabase _coreDatabase;
	BukkitTask _updateServerDataTask;
	BukkitTask _updateServerCacheTask;

	public CorePortal(HexusPlugin plugin) {
		super(plugin, "Portal");

		_createdMillis = System.currentTimeMillis();
		_callbacks = new HashMap<>();
		_serverGroupCache = new HashSet<>();
		_serverCache = new HashSet<>();
		_networkChannelSpies = new HashSet<>();

		try {
			_serverName = plugin.readFile(new File("_name.dat"))[0];
			_serverGroupName = plugin.readFile(new File("_group.dat"))[0];
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		_messenger = _hexusPlugin.getServer().getMessenger();
		_messenger.registerOutgoingPluginChannel(_hexusPlugin, PROXY_CHANNEL);
		_messenger.registerIncomingPluginChannel(_hexusPlugin, PROXY_CHANNEL, this);

		PermissionGroup._PLAYER._permissions.addAll(List.of(PERM.COMMAND_SERVER,
			PERM.COMMAND_PERFORMANCE,
			PERM.COMMAND_MOTD,
			PERM.COMMAND_MOTD_VIEW));
		PermissionGroup.VIP._permissions.add(PERM.BYPASS_FULL_PLAYER);
		PermissionGroup.MVP._permissions.add(PERM.COMMAND_HOSTSERVER);
		PermissionGroup.TRAINEE._permissions.addAll(List.of(PERM.BYPASS_FULL_STAFF, PERM.COMMAND_SEND_ALERTS));
		PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOSTEVENT);
		PermissionGroup.ADMINISTRATOR._permissions.addAll(List.of(PERM.COMMAND_SEND,
			PERM.COMMAND_MOTD_SET,
			PERM.COMMAND_NETWORK,
			PERM.COMMAND_NETWORK_SPY,
			PERM.COMMAND_NETWORK_GROUP,
			PERM.COMMAND_NETWORK_GROUP_CREATE,
			PERM.COMMAND_NETWORK_GROUP_DELETE,
			PERM.COMMAND_NETWORK_GROUP_LIST,
			PERM.COMMAND_NETWORK_GROUP_RESTART,
			PERM.COMMAND_NETWORK_SERVER,
			PERM.COMMAND_NETWORK_SERVER_RESTART,
			PERM.COMMAND_NETWORK_SERVER_RESTART));
	}

	@Override
	public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
		_coreActionBar = (CoreActionBar) dependencies.get(CoreActionBar.class);
		_coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
		_coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
		_corePermission = (CorePermission) dependencies.get(CorePermission.class);

		// Run this sync so we at least have an initial copy of server & group data
		updateServerData();
		updateServerCache();
	}

	@Override
	public void onEnable() {
		_coreCommand.register(new CommandServer(this, _coreDatabase));
		_coreCommand.register(new CommandSend(this, _coreActionBar));
		_coreCommand.register(new CommandHostEvent(this, _coreDatabase));
		_coreCommand.register(new CommandHostServer(this, _coreDatabase));
		_coreCommand.register(new CommandNetwork(this, _coreDatabase));

		_coreDatabase._database.registerConsumer("*",
			(_, channelName, message) -> _networkChannelSpies.forEach(commandSender -> commandSender.sendMessage(
				F.fSub(channelName, message))));

		_coreDatabase._database.registerConsumer(PortalTeleportMessage.CHANNEL_NAME,
			(_, _, rawMessage) -> _hexusPlugin.runAsync(() -> {
				PortalTeleportMessage message = PortalTeleportMessage.parse(rawMessage);

				_hexusPlugin.getServer()
					.getOnlinePlayers()
					.stream()
					.filter(player -> player.getUniqueId().equals(message._targetUUID))
					.forEach(targetPlayer -> teleport(targetPlayer, message._serverName));

				if (message._senderUUID == null) return;

				AtomicReference<OfflinePlayer> atomicTarget = new AtomicReference<>();
				AtomicReference<OfflinePlayer> atomicSender = new AtomicReference<>();

				AtomicInteger remainingTaskCount = new AtomicInteger(2);
				Consumer<String> taskCompleted = (task) -> {
					int newRemainingTaskCount = remainingTaskCount.decrementAndGet();
					if (newRemainingTaskCount > 0) return;

					OfflinePlayer target = atomicTarget.get();
					OfflinePlayer sender = atomicSender.get();

					_hexusPlugin.getServer()
						.getOnlinePlayers()
						.stream()
						.filter(staff -> staff.hasPermission(PERM.COMMAND_SEND_ALERTS.name()))
						.forEach(staff -> {
							staff.sendMessage(F.fStaff(this,
								F.fItem(sender == null ?
									"<UNKNOWN>" :
									sender.getName()),
								" sent ",
								F.fItem(target == null ?
									"<UNKNOWN>" :
									target.getName()),
								" to ",
								F.fItem(message._serverName),
								"."));
							staff.playSound(staff.getLocation(),
								Sound.NOTE_PLING,
								Float.MAX_VALUE,
								2);
						});
				};

				BukkitTask[] tasks = new BukkitTask[]{_hexusPlugin.runAsync(() -> {
					try {
						atomicTarget.set(PlayerSearch.offlinePlayerSearch(message._targetUUID));
					} catch (Exception ex) {
						logSevere(ex);
					}
					taskCompleted.accept("target");
				}), _hexusPlugin.runAsync(() -> {
					try {
						atomicSender.set(PlayerSearch.offlinePlayerSearch(message._senderUUID));
					} catch (Exception ex) {
						logSevere(ex);
					}
					taskCompleted.accept("sender");
				})};
			}));

		_coreDatabase._database.registerConsumer(PortalRestartServerGroupMessage.CHANNEL_NAME,
			(_, _, rawMessage) -> {
				PortalRestartServerGroupMessage message =
					PortalRestartServerGroupMessage.fromString(rawMessage);
				if (!message._groupName().equals(_serverGroupName) &&
					!message._groupName().equals("*")) {
					return;
				}
				_hexusPlugin.getServer().shutdown();
			});

		_coreDatabase._database.registerConsumer(PortalRestartServerMessage.CHANNEL_NAME,
			(_, _, rawMessage) -> {
				PortalRestartServerMessage message = PortalRestartServerMessage.fromString(rawMessage);
				if (!message._serverName().equals(_serverName) && !message._serverName().equals("*")) {
					return;
				}
				_hexusPlugin.getServer().shutdown();
			});

		_updateServerCacheTask = _hexusPlugin.runAsyncTimer(this::updateServerCache, 0, 20);
		_updateServerDataTask = _hexusPlugin.runAsyncTimer(this::updateServerData, 0, 20);
	}

	@Override
	public void onDisable() {
		_messenger.unregisterOutgoingPluginChannel(_hexusPlugin, PROXY_CHANNEL);
		_messenger.unregisterIncomingPluginChannel(_hexusPlugin, PROXY_CHANNEL);

		_callbacks.clear();

		if (_updateServerDataTask != null) {
			_updateServerDataTask.cancel();
		}
		if (_updateServerCacheTask != null) {
			_updateServerCacheTask.cancel();
		}

		Server server = _hexusPlugin.getServer();

		ServerListPingEvent ping =
			new ServerListPingEvent(new InetSocketAddress(server.getIp(), server.getPort()).getAddress(),
				server.getMotd(),
				server.getOnlinePlayers().size(),
				server.getMaxPlayers());
		server.getPluginManager().callEvent(ping);

		new ServerData(_serverName,
			server.getIp(),
			ping.getMaxPlayers(),
			_createdMillis,
			_serverGroupName,
			"DEAD",
			ping.getNumPlayers(),
			server.getPort(),
			20,
			System.currentTimeMillis(),
			false).update(_coreDatabase._database._jedis);
	}

	public void updateServerData() {
		try {
			Server server = _hexusPlugin.getServer();

			ServerListPingEvent ping = new ServerListPingEvent(new InetSocketAddress(server.getIp(),
				server.getPort()).getAddress(),
				server.getMotd(),
				server.getOnlinePlayers().size(),
				server.getMaxPlayers());
			server.getPluginManager().callEvent(ping);

			OptionalDouble averageTps =
				OptionalDouble.of(20); //Arrays.stream(MinecraftServer.getServer().recentTps).average();

			new ServerData(_serverName,
				server.getIp(),
				server.getMaxPlayers(),
				_createdMillis,
				_serverGroupName,
				ping.getMotd(),
				ping.getNumPlayers(),
				server.getPort(),
				averageTps.orElse(2D),
				System.currentTimeMillis(),
				false).update(_coreDatabase._database._jedis);
		} catch (JedisException ex) {
			logSevere(ex);
		}
	}

	public void updateServerCache() throws JedisException {
		try {
			ServerGroupData[] groupCache = ServerQueries.getServerGroups(_coreDatabase._database._jedis);
			ServerData[] serverCache = ServerQueries.getServers(_coreDatabase._database._jedis);

			_serverGroupCache.clear();
			_serverGroupCache.addAll(Arrays.asList(groupCache));
			_serverCache.clear();
			_serverCache.addAll(Arrays.asList(serverCache));
		} catch (JedisException ex) {
			logSevere(ex);
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
		//noinspection UnstableApiUsage
		ByteArrayDataInput in = ByteStreams.newDataInput(message);

		if (channel.equals(PROXY_CHANNEL)) {
			String subChannel = in.readUTF();
			if (!_callbacks.containsKey(subChannel)) {
				return;
			}
			_callbacks.get(subChannel).forEach((_, callback) -> {
				callback.setIn(in);
				callback.run();
			});
		}
	}

	public void teleport(Player player, String serverName) {
		if (serverName.equalsIgnoreCase(_serverName)) {
			player.sendMessage(F.fMain(this,
				F.fError("You are already connected to ", F.fItem(serverName), ".")));
			return;
		}

		player.sendMessage(F.fMain(this,
			"You were sent from ",
			F.fItem(_serverName),
			" to ",
			F.fItem(serverName),
			"."));

		// TODO: Change from bungeecord channels to redis channels. Implement behaviour on proxy.
		//noinspection UnstableApiUsage
		//        ByteArrayDataOutput out = ByteStreams.newDataOutput();
		//        out.writeUTF("Connect");
		//        out.writeUTF(serverName);
		//        player.sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, out.toByteArray());
		sendProxyMessage("ConnectOther", player.getName(), serverName);
	}

	public void teleportAsync(UUID targetUUID, String serverName, UUID senderUUID) {
		_coreDatabase._database._jedis.publish(PortalTeleportMessage.CHANNEL_NAME,
			new PortalTeleportMessage(targetUUID, serverName, senderUUID).stringify());
	}

	public void teleportAsync(UUID targetUUID, String serverName) {
		teleportAsync(targetUUID, serverName, null);
	}

	public void teleportPlayerToRandomServer(Player player, String serverGroupName) {
		ServerData[] availableServers = _serverCache.stream()
			.filter(serverData -> serverData._group.equalsIgnoreCase(
				serverGroupName))
			.filter(serverData -> !serverData._updatedByMonitor)
			.filter(serverData -> !serverData._id.equalsIgnoreCase(
				_serverName))
			.toArray(ServerData[]::new);
		if (availableServers.length == 0) {
			player.sendMessage(F.fMain(this,
				F.fError("Sorry, we were unable to locate a ",
					F.fItem(serverGroupName),
					" server. Please try again later or contact an administrator if this " +
						"issue persists.")));
			return;
		}

		teleport(player, availableServers[new Random().nextInt(availableServers.length)]._id);
	}

	public ServerData[] getServers() {
		return _serverCache.stream()
			.filter(serverData -> !serverData._updatedByMonitor)
			.toArray(ServerData[]::new);
	}

	public ServerData[] getServers(String serverGroupName) {
		return Arrays.stream(getServers())
			.filter(serverData -> serverData._group.equalsIgnoreCase(serverGroupName))
			.toArray(ServerData[]::new);
	}

	public String[] getServerNames() {
		return Arrays.stream(getServers())
			.map(serverData -> serverData._id)
			.sorted(Comparator.comparing(String::toLowerCase))
			.toArray(String[]::new);
	}

	public String[] getServerNames(String serverGroupName) {
		return Arrays.stream(getServers(serverGroupName))
			.map(serverData -> serverData._id)
			.toArray(String[]::new);
	}

	public ServerData getServer(String serverName) {
		return Arrays.stream(getServers())
			.filter(serverData -> serverData._id.equalsIgnoreCase(serverName))
			.findAny()
			.orElse(null);
	}

	public ServerGroupData[] getServerGroups() {
		return _serverGroupCache.toArray(ServerGroupData[]::new);
	}

	public ServerGroupData getServerGroup(String serverGroupName) {
		return Arrays.stream(getServerGroups())
			.filter(serverGroupData -> serverGroupData._id.equalsIgnoreCase(serverGroupName))
			.findAny()
			.orElse(null);
	}

	public String[] getServerGroupNames() {
		return _serverGroupCache.stream()
			.map(serverGroupData -> serverGroupData._id)
			.sorted(Comparator.comparing(String::toLowerCase))
			.toArray(String[]::new);
	}

	public BukkitTask restartServerAsync(String serverName) {
		return _hexusPlugin.runAsync(() -> _coreDatabase._database._jedis.publish(PortalRestartServerMessage.CHANNEL_NAME,
			new PortalRestartServerMessage(serverName).toString()));
	}

	public BukkitTask restartServerGroupAsync(String groupName) {
		return _hexusPlugin.runAsync(() -> _coreDatabase._database._jedis.publish(
			PortalRestartServerGroupMessage.CHANNEL_NAME,
			new PortalRestartServerGroupMessage(groupName).toString()));
	}

	public UUID registerCallback(String channelName, ByteArrayDataInputRunnable callback) {
		UUID id = UUID.randomUUID();
		if (!_callbacks.containsKey(channelName)) {
			_callbacks.put(channelName, new HashMap<>());
		}
		_callbacks.get(channelName).put(id, callback);
		return id;
	}

	public void unregisterCallback(UUID id) {
		_callbacks.forEach((s, uuidRunnableMap) -> {
			if (!uuidRunnableMap.containsKey(id)) {
				return;
			}
			uuidRunnableMap.remove(id);
		});
	}

	public void sendProxyMessage(String... data) {
		// TODO: Change from BungeeCord channels to redis channels. Implement behaviour on proxy.
		//noinspection UnstableApiUsage
		ByteArrayDataOutput outServer = ByteStreams.newDataOutput();
		for (String s : data) {
			outServer.writeUTF(s);
		}
		_hexusPlugin.getServer().sendPluginMessage(_hexusPlugin, PROXY_CHANNEL, outServer.toByteArray());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) {
			return;
		}
		if (!event.getPlayer().hasPermission(PERM.BYPASS_FULL_PLAYER.name())) {
			return;
		}
		event.setResult(PlayerLoginEvent.Result.ALLOWED);
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		event.setJoinMessage(F.fSub("Join",
			(player.hasPermission(CoreChat.PERM.CHAT_PREFIX.name()) ?
				F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(
					player)._groups()), true, true) + C.fReset + " " :
				""),
			event.getPlayer().getName()));
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		_networkChannelSpies.remove(event.getPlayer());
		event.setQuitMessage(F.fSub("Quit",
			(player.hasPermission(CoreChat.PERM.CHAT_PREFIX.name()) ?
				F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(
					player)._groups()), true, true) + C.fReset + " " :
				""),
			event.getPlayer().getName()));
	}

	public void openServerGroupCreateGui(Player player) {
		Gui gui = new GuiCreateServerGroup(player, new ServerGroupData.Builder());
		player.openInventory(gui._inventory);
	}

	public enum PERM implements IPermission {
		COMMAND_SEND,
		COMMAND_SEND_ALERTS,
		COMMAND_SERVER,
		COMMAND_MOTD,
		COMMAND_MOTD_VIEW,
		COMMAND_MOTD_SET,
		COMMAND_HOSTSERVER,
		COMMAND_HOSTEVENT,
		COMMAND_PERFORMANCE,

		COMMAND_NETWORK,
		COMMAND_NETWORK_GROUP,
		COMMAND_NETWORK_GROUP_CREATE,
		COMMAND_NETWORK_GROUP_DELETE,
		COMMAND_NETWORK_GROUP_LIST,
		COMMAND_NETWORK_GROUP_RESTART,

		COMMAND_NETWORK_SERVER,
		COMMAND_NETWORK_SERVER_RESTART,
		COMMAND_NETWORK_SERVER_LIST,

		COMMAND_NETWORK_SPY,

		COMMAND_NETWORK_DATABASE,
		COMMAND_NETWORK_DATABASE_SPY,

		COMMAND_LOCATE,
		COMMAND_LOCATE_SERVER,
		COMMAND_LOCATE_PLAYER,

		BYPASS_FULL_PLAYER,
		BYPASS_FULL_STAFF
	}
}
