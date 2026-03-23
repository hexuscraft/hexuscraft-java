package net.hexuscraft.arcade.host;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.host.command.CommandHost;
import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ArcadeHost extends MiniPlugin<Arcade> {

    public enum PERM implements IPermission {
        COMMAND_HOST,
        COMMAND_HOST_SET,
        COMMAND_HOST_VIEW
    }

    public final AtomicReference<OfflinePlayer> _host;
    private final Long MAX_HOST_LAST_SEEN_MILLIS = Duration.ofMinutes(5)
            .toMillis();
    private final AtomicReference<BukkitTask> _hostAbandonedTask;
    private final AtomicLong _hostLastSeenMillis;
    private CoreCommand _coreCommand;
    private CoreDatabase _coreDatabase;
    private CorePortal _corePortal;
    private PermissionAttachment _hostPermissionAttachment;

    public ArcadeHost(final Arcade arcade) {
        super(arcade, "Server Host");

        _host = new AtomicReference<>();
        _hostAbandonedTask = new AtomicReference<>();
        _hostLastSeenMillis = new AtomicLong(System.currentTimeMillis());

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HOST);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_HOST_VIEW);
        PermissionGroup.EVENT_LEAD._permissions.add(PERM.COMMAND_HOST_SET);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable() {
        _coreCommand.register(new CommandHost(this));

        _hexusPlugin.runAsync(() -> {
            try {
                _host.set(PlayerSearch.offlinePlayerSearch(
                        _corePortal.getServerGroup(_corePortal._serverGroupName)._hostUniqueId));
            } catch (final IOException ex) {
                logSevere(ex);
            }

            // We want to wait a little bit before attempting to teleport the server host so the proxies and notchians have time to update their server cache
            _hexusPlugin.runAsyncLater(() -> {
                if (_host.get() == null) return;
                _corePortal.teleportAsync(_host.get()
                        .getUniqueId(), _corePortal._serverName);
            }, 40L);
        });

        _hostAbandonedTask.set(_hexusPlugin.runSyncTimer(() -> {
            // We want to run the host check even if the server was started with no host, as an admin can become the host of any existing server.
            final OfflinePlayer host = _host.get();
            if (host == null) return;
            if (host.isOnline()) return;
            if ((System.currentTimeMillis() - _hostLastSeenMillis.get()) < MAX_HOST_LAST_SEEN_MILLIS) return;

            _hostAbandonedTask.get()
                    .cancel();
            _hexusPlugin.getServer()
                    .broadcastMessage(F.fMain(this,
                            "The host has abandoned their server. You will be sent back to a lobby. Thanks for playing!"));
            _hexusPlugin.getServer()
                    .getOnlinePlayers()
                    .forEach(player -> {
                        //noinspection deprecation
                        player.sendTitle(C.cYellow + "Server Abandoned", "Sending you back to a lobby...");
                        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, Float.MAX_VALUE, 1);
                    });
            _coreDatabase._database._jedis.del(ServerQueries.SERVERGROUP(_corePortal._serverGroupName));
        }, 0, 20L));
    }

    public void setHost(final OfflinePlayer newHost) {
        final OfflinePlayer oldHost = _host.getAndSet(newHost);

        if (_hostPermissionAttachment != null) _hostPermissionAttachment.remove();

        if (oldHost.isOnline()) oldHost.getPlayer()
                .sendMessage(F.fMain(this, F.fError("You are no longer the server host.")));

        if (!newHost.isOnline()) return;
        final Player newHostPlayer = newHost.getPlayer();
        _hostPermissionAttachment = newHostPlayer.addAttachment(_hexusPlugin);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_SET.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_START.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_STOP.name(), true);
        newHostPlayer.sendMessage(F.fMain(this, F.fSuccess("You are now the server host.")));
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!_host.get()
                .equals(player)) return;

        _hostPermissionAttachment = player.addAttachment(_hexusPlugin);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_SET.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_START.name(), true);
        _hostPermissionAttachment.setPermission(ArcadeManager.PERM.COMMAND_GAME_STOP.name(), true);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (!event.getPlayer()
                .equals(_host.get())) return;
        _hostLastSeenMillis.set(System.currentTimeMillis());

        if (_hostPermissionAttachment == null) return;
        _hostPermissionAttachment.remove();
        _hostPermissionAttachment = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if (!event.getPlayer()
                .equals(_host.get())) return;
        event.setFormat(" " + C.cAqua + C.fBold + "HOST" + C.fReset + " " + event.getFormat());
    }

}
