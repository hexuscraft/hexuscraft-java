package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public final class MiniPluginAntiCheat extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        CHEAT_ALERTS
    }

    private MiniPluginPortal _miniPluginPortal;

    private final Map<Player, Map<String, Integer>> _violations;

    public MiniPluginAntiCheat(final HexusPlugin plugin) {
        super(plugin, "Anti Cheat");

        _violations = new HashMap<>();

        PermissionGroup.TRAINEE._permissions.add(PERM.CHEAT_ALERTS);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
    }

    @Override
    public void onEnable() {
        _hexusPlugin.getServer().getOnlinePlayers().forEach(player -> onPlayerJoin(new PlayerJoinEvent(player, null)));
    }

    @Override
    public void onDisable() {
        _violations.clear();
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        _violations.put(event.getPlayer(), new HashMap<>());
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        _violations.remove(event.getPlayer());
    }

    @EventHandler
    private void onPlayerInteractAtEntity(final PlayerInteractAtEntityEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        final double distance = player.getLocation().distance(event.getRightClicked().getLocation());
        if (distance > 4) event.setCancelled(true);
        if (distance > 5.5) {
            flag(player, "Reach", CheatSeverity.HIGH);
        } else if (distance > 5) {
            flag(player, "Reach", CheatSeverity.MEDIUM);
        } else if (distance > 4.5) {
            flag(player, "Reach", CheatSeverity.LOW);
        }
    }

    @EventHandler
    private void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = ((Player) event.getDamager()).getPlayer();
        double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());

        if (distance > 4) {
            event.setCancelled(true);
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (distance > 5.5) {
            flag(player, "Reach", CheatSeverity.HIGH);
        } else if (distance > 5) {
            flag(player, "Reach", CheatSeverity.MEDIUM);
        } else if (distance > 4.5) {
            flag(player, "Reach", CheatSeverity.LOW);
        }
    }

    public void alert(final Player player, final String reason, final CheatSeverity severity, final int count) {
        _hexusPlugin.getServer()
                .getOnlinePlayers()
                .stream()
                .filter(staff -> staff.hasPermission(PERM.CHEAT_ALERTS.name()))
                .forEach(staff -> staff.sendMessage(F.fCheat(player, severity, reason, count, _miniPluginPortal._serverName)));
    }

    public void kick(final Player player, final String reason) {
        player.kickPlayer(F.fPunishKick("Automatic cheat detection - " + reason));
    }

    public void flag(final Player player, final String reason, final CheatSeverity severity) {
        final String keyName = reason + ":" + severity.name();

        final Map<String, Integer> playerViolations = _violations.get(player);

        if (!playerViolations.containsKey(keyName)) {
            playerViolations.put(keyName, 0);
        }
        final int newCount = playerViolations.get(keyName) + 1;
        playerViolations.put(keyName, newCount);

        alert(player, reason, severity, newCount);
        if (newCount == 10) {
            kick(player, reason);
        }
    }

}