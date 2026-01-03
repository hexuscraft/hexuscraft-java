package net.hexuscraft.arcade.host;

import net.hexuscraft.arcade.Arcade;
import net.hexuscraft.arcade.host.command.CommandSetHost;
import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MiniPluginHost extends MiniPlugin<Arcade> {

    private final Long MAX_HOST_LAST_SEEN_MILLIS = Duration.ofMinutes(5).toMillis();

    public AtomicReference<UUID> _hostUniqueId;
    private MiniPluginCommand _miniPluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPortal _miniPluginPortal;
    private AtomicLong _hostLastSeenMillis;

    public MiniPluginHost(final Arcade arcade) {
        super(arcade, "Server Host");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_SET_HOST);
        _hostUniqueId = new AtomicReference<>(UtilUniqueId.EMPTY_UUID);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _hostLastSeenMillis = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public void onEnable() {
        _miniPluginCommand.register(new CommandSetHost(this));

        _hexusPlugin.runAsync(() -> {
            try {
                _hostUniqueId.set(Objects.requireNonNull(
                        ServerQueries.getServerGroup(_miniPluginDatabase.getUnifiedJedis(),
                                _miniPluginPortal._serverGroupName))._hostUniqueId);
            } catch (final JedisException | NullPointerException ex) {
                ex.printStackTrace();
            }

            // We want to wait a few seconds before attempting to teleport the server host so the proxies and notchians have time to update their server cache
            _hexusPlugin.runAsyncLater(() -> {
                if (_hostUniqueId.get().equals(UtilUniqueId.EMPTY_UUID)) return;
                _miniPluginPortal.teleportAsync(_hostUniqueId.get(), _miniPluginPortal._serverName);
            }, 60L);
        });

        _hexusPlugin.runSyncTimer(() -> {
            // We want to run the host check even if the server was started with no host, as an admin can become the host of any existing server.
            if (_hostUniqueId.get().equals(UtilUniqueId.EMPTY_UUID)) return;

            final boolean isHostOnline = _hexusPlugin.getServer().getOnlinePlayers().stream()
                    .anyMatch(player -> player.getUniqueId().equals(_hostUniqueId.get()));
            if (isHostOnline) return;
            if ((System.currentTimeMillis() - _hostLastSeenMillis.get()) < MAX_HOST_LAST_SEEN_MILLIS) return;

            _hexusPlugin.getServer()
                    .broadcastMessage(F.fMain(this, "The host has abandoned this server. Thanks for playing!"));
            _hexusPlugin.getServer().getOnlinePlayers().forEach(
                    player -> player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, Float.MAX_VALUE, 1));
            _miniPluginDatabase.getUnifiedJedis().srem(ServerQueries.SERVERGROUP(_miniPluginPortal._serverGroupName));
        }, 0, 20L);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(_hostUniqueId.get())) return;
        _hostLastSeenMillis.set(System.currentTimeMillis());
    }

    public enum PERM implements IPermission {
        COMMAND_SET_HOST
    }

}
