package net.hexuscraft.arcade.host;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.host.command.CommandHost;
import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;

public class ArcadeHost extends MiniPlugin<Arcade> {

	final Long MAX_HOST_LAST_SEEN_MILLIS = Duration.ofMinutes(5).toMillis();
	public OfflinePlayer _host;
	PermissionAttachment _hostAttachment;
	BukkitTask _hostAbandonedTask;
	long _hostLastSeenMillis;
	CoreCommand _coreCommand;
	CoreDatabase _coreDatabase;
	CorePortal _corePortal;

	public ArcadeHost(Arcade arcade) {
		super(arcade, "Server Host");

		PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HOST);
		PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HOST_VIEW);
		PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOST_SET);
	}

	@Override
	public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
		_coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
		_coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
		_corePortal = (CorePortal) dependencies.get(CorePortal.class);
	}

	@Override
	public void onEnable() {
		_coreCommand.register(new CommandHost(this));

		_hexusPlugin.runAsyncLater(() -> {
			ServerGroupData serverGroupData = _corePortal.getServerGroup(_corePortal._serverGroupName);
			if (serverGroupData._hostUUID == null) return;
			if (serverGroupData._hostUUID.equals(UtilUniqueId.EMPTY_UUID))
				return; // legacy server group data. new entries should nullify this value.

			try {
				_host = PlayerSearch.offlinePlayerSearch(serverGroupData._hostUUID);
			} catch (URISyntaxException | IOException ex) {
				logSevere(ex);
				return;
			}
			if (_host == null) return;
			_corePortal.teleportAsync(_host.getUniqueId(), _corePortal._serverName);
		}, 20L);

		_hostAbandonedTask = _hexusPlugin.runSyncTimer(() -> {
			// We want to run the host check even if the server was started with no host, as an admin can become the
			// host of any existing server.
			if (_host == null) return;

			if (_host.isOnline()) _hostLastSeenMillis = System.currentTimeMillis();
			if ((System.currentTimeMillis() - _hostLastSeenMillis) < MAX_HOST_LAST_SEEN_MILLIS) return;

			_hostAbandonedTask.cancel();
			_hexusPlugin.getServer().broadcastMessage(F.fMain(this, "The host has abandoned their server. You will be sent back to a lobby. Thanks for playing!"));
			_hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
				//noinspection deprecation
				player.sendTitle(C.cYellow + "Server Abandoned", "Sending you back to a lobby...");
				player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, Float.MAX_VALUE, 1);
			});
			_coreDatabase._database._jedis.del(ServerQueries.SERVERGROUP(_corePortal._serverGroupName));
		}, 0, 20L);
	}

	@Override
	public void onDisable() {
		_hostAbandonedTask.cancel();
	}

	public void refreshHostPermissions() {
		if (_hostAttachment != null) {
			_hostAttachment.remove();
			_hostAttachment = null;
		}

		if (_host == null) return;
		if (!_host.isOnline()) return;

		_hostAttachment = _host.getPlayer().addAttachment(_hexusPlugin);
		_hostAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME.name(), true);
		_hostAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_SET.name(), true);
		_hostAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_START.name(), true);
		_hostAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_STOP.name(), true);
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		if (!event.getPlayer().equals(_host)) return;
		refreshHostPermissions();
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		if (!event.getPlayer().equals(_host)) return;
		refreshHostPermissions();
	}

	@EventHandler(priority = EventPriority.HIGH)
	void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().equals(_host)) return;
		event.setFormat(" " + C.cAqua + C.fBold + "HOST" + C.fReset + " " + event.getFormat());
	}

	public enum PERM implements IPermission {
		COMMAND_HOST, COMMAND_HOST_SET, COMMAND_HOST_VIEW
	}

}
