package net.hexuscraft.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.query.ProxyQueryEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.QueryResponse;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.hexuscraft.common.database.Database;
import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.messages.PunishAppliedMessage;
import net.hexuscraft.common.database.queries.PunishQueries;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PunishType;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id = "hexuscraft-proxy", name = "Proxy", version = "1.0.0")
public class Proxy {

	final String MOTD_PREFIX = String.join("\n", "        §6§lHexuscraft§r §f§lNetwork§r  §3[1.8+]§r", " §f§l▶§r ");

	final Database _database;
	final ProxyServer _server;
	final Logger _logger;

	String _motd;
	int _playerCount;
	int _capacityCount;
	Map<String, ServerData> _servers;
	Map<String, ServerGroupData> _serverGroups;

	@Inject
	public Proxy(ProxyServer server, Logger logger) {
		_database = new Database();
		_server = server;
		_logger = logger;
		_motd = MOTD_PREFIX + "<insert funny message here>";
		_playerCount = 0;
		_capacityCount = 0;
		_servers = new HashMap<>();
		_serverGroups = new HashMap<>();
	}

	@Subscribe
	void onProxyInitialize(ProxyInitializeEvent event) {
		CommandManager commandManager = _server.getCommandManager();
		commandManager.getAliases().forEach(commandManager::unregister);

		_server.getScheduler()
			.buildTask(this, this::updateMOTD)
			.repeat(Duration.ofSeconds(5))
			.delay(Duration.ofSeconds(0))
			.schedule();
		_server.getScheduler()
			.buildTask(this, this::updateServers)
			.repeat(Duration.ofSeconds(1))
			.delay(Duration.ofSeconds(0))
			.schedule();

		_database.registerConsumer(PunishAppliedMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
			_server.getScheduler().buildTask(this, () -> {
				PunishData punishData;
				try {
					punishData =
						new PunishData(new HashMap<>(_database._jedis.hgetAll(PunishQueries.PUNISHMENT(
							PunishAppliedMessage.fromString(rawMessage)._uuid))));
				} catch (JedisException ex) {
					_logger.severe(ex.getMessage());
					return;
				}

				if (punishData._type != PunishType.BAN) return;
				Player target = _server.getPlayer(punishData._targetUUID).orElse(null);
				if (target == null) return;
				target.disconnect(Component.text(F.fPunish(punishData)));
			});
		});
	}

	void updateMOTD() {
		_motd = MOTD_PREFIX + ServerQueries.getMotd(_database._jedis);
	}

	void updateServers() {
		_servers = ServerQueries.getServersAsMap(_database._jedis);
		_serverGroups = ServerQueries.getServerGroupsAsMap(_database._jedis);

		_playerCount = 0;
		_capacityCount = 0;

		// Unregister non-existing servers
		for (RegisteredServer server : _server.getAllServers()) {
			ServerInfo info = server.getServerInfo();
			if (_servers.containsKey(info.getName())) continue;
			_server.unregisterServer(info);
		}

		_server.getConfiguration().getAttemptConnectionOrder().clear();

		// Register non-registered servers
		for (ServerData server : _servers.values()) {
			_playerCount += server._players;
			_capacityCount += server._capacity;

			if (_server.getServer(server._id).isEmpty()) _server.registerServer(new ServerInfo(server._id,
				new InetSocketAddress(server._address, server._port)));

			ServerGroupData group = _serverGroups.get(server._group);
			if (group == null) continue;
			if (!group._fallback) continue;
			_server.getConfiguration().getAttemptConnectionOrder().add(server._id);
		}
	}

	@Subscribe
	void onProxyQuery(ProxyQueryEvent event) {
		QueryResponse.Builder builder = QueryResponse.builder();
		builder.players("§r",
			"    §6§lHexuscraft§r §f§lNetwork§r    ",
			"§r",
			"  §f§l▶§r  Mini Games",
			"  §f§l▶§r  Private Servers",
			"  §f§l▶§r  Tournaments",
			"§r");
		builder.clearPlugins();
		builder.proxyVersion("Minecraft 1.8+");
		builder.gameVersion("Minecraft 1.8+");
		builder.currentPlayers(_playerCount);
		builder.maxPlayers(_capacityCount);
		event.setResponse(builder.build());
	}

	@Subscribe
	void onProxyPing(ProxyPingEvent event) {
		ServerPing.Builder builder = ServerPing.builder();

		builder.onlinePlayers(_playerCount);
		builder.maximumPlayers(_capacityCount);

		builder.description(Component.text(_motd));
		builder.samplePlayers(new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("    §6§lHexuscraft§r §f§lNetwork§r    ", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("  §f§l▶§r  Mini Games", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("  §f§l▶§r  Private Servers", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("  §f§l▶§r  Tournaments", new UUID(0L, 0L)),
			new ServerPing.SamplePlayer("§r", new UUID(0L, 0L)));
		builder.version(new ServerPing.Version(Math.max(ProtocolVersion.MINECRAFT_1_8.getProtocol(),
			event.getConnection().getProtocolVersion().getProtocol()), "Minecraft 1.8"));
		try {
			builder.favicon(Favicon.create(Path.of("server-icon.png")));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		event.setPing(builder.build());
	}

	@Subscribe
	void onLogin(LoginEvent event) {
		try {
			UnifiedJedis jedis = _database._jedis;
			Set<UUID> punishmentIds =
				jedis.smembers(PunishQueries.RECEIVED(event.getPlayer().getUniqueId()))
					.stream()
					.map(UUID::fromString)
					.collect(Collectors.toSet());

			// We want to display the longest ban remaining.
			// If there are multiple bans with the same remaining time (usually multiple perm bans), display the most
			// recent ban.
			// If there are multiple bans matching this and were also applied at the EXACT same time (??), fate
			// decides the displayed message.

			Set<PunishData> activePunishments = new HashSet<>();

			for (UUID punishmentUniqueId : punishmentIds) {
				try {
					Map<String, String> rawData =
						new HashMap<>(jedis.hgetAll(PunishQueries.PUNISHMENT(punishmentUniqueId)));
					rawData.put("uuid", punishmentUniqueId.toString());

					PunishData punishData = new PunishData(rawData);

					if (!punishData._active) {
						continue;
					}

					if (punishData._length != -1) {
						long remaining = punishData.getRemaining();
						if (remaining <= 0) {
							_database._jedis.hset(PunishQueries.PUNISHMENT(
									punishmentUniqueId),
								Map.of("active",
									"false",
									"removeOrigin",
									Long.toString(System.currentTimeMillis()),
									"removeReason",
									"EXPIRED",
									"removeTargetServer",
									"Proxy-" + _server.getBoundAddress().toString(),
									"removeStaffUUID",
									UtilUniqueId.EMPTY_UUID.toString(),
									"removeStaffServer",
									"Proxy-" +
										_server.getBoundAddress().toString()));
							continue;
						}
					}

					if (!punishData._type.equals(PunishType.BAN)) {
						continue;
					}

					activePunishments.add(punishData);
				} catch (JedisException ex) {
					_logger.warning("Error while checking punish punish for '" +
						event.getPlayer().getUsername() + "': " + ex.getMessage());
				}
			}

			if (activePunishments.isEmpty()) {
				return;
			}

			AtomicReference<PunishData> punishData = new AtomicReference<>();
			if (activePunishments.size() > 1) {
				for (PunishData data : activePunishments) {
					if (punishData.get() == null) {
						punishData.set(data);
						continue;
					}
					punishData.set(punishData.get().compare(data));
				}
			} else {
				punishData.set(activePunishments.iterator().next());
			}

			event.setResult(ResultedEvent.ComponentResult.denied(Component.text(F.fPunish(punishData.get()))));
		} catch (JedisException ex) {
			_logger.warning(
				"Error while fetching punishment punish for '" + event.getPlayer().getUsername() +
					"': " + ex.getMessage());
		}

	}

	@Subscribe
	void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
		Player player = event.getPlayer();

//        if (player.getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_8.getProtocol()) {
		if (player.getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_7_6.getProtocol()) {
			event.setInitialServer(null);
			player.disconnect(Component.text()
				.color(NamedTextColor.RED)
				.append(Component.text("Your game client is too outdated."))
				.append(Component.text(
					"\nPlease use Minecraft 1.8 or newer to join Hexuscraft.",
					NamedTextColor.GRAY))
				.append(Component.text("\n\nwww.hexuscraft.net",
					NamedTextColor.YELLOW))
				.build());
			return;
		}

		RegisteredServer[] fallbackServers = _server.getConfiguration()
			.getAttemptConnectionOrder()
			.stream()
			.map(_server::getServer)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toArray(RegisteredServer[]::new);

		if (fallbackServers.length == 0) {
			player.disconnect(Component.text()
				.color(NamedTextColor.RED)
				.append(Component.text(
					"There are currently no fallback servers available."))
				.append(Component.text("\nPlease try again later.",
					NamedTextColor.GRAY))
				.append(Component.text("\n\nwww.hexuscraft.net",
					NamedTextColor.YELLOW))
				.build());
			return;
		}

		event.setInitialServer(fallbackServers[new Random().nextInt(fallbackServers.length)]);
	}

}
