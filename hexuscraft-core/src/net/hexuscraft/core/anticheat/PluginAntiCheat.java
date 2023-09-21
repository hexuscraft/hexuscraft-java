package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.entity.NBTEditor;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class PluginAntiCheat extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_TESTBAN
    }

    private PluginPortal _pluginPortal;

    private final Map<Player, Map<String, Integer>> _violations;

    private final Map<Player, List<Guardian>> _guardians;

    public PluginAntiCheat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Anti Cheat");

        _violations = new HashMap<>();
        _guardians = new HashMap<>();

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_TESTBAN);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginPortal = (PluginPortal) dependencies.get(PluginPortal.class);
    }

    @Override
    public final void onEnable() {
        for (Player player : _javaPlugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Override
    public final void onDisable() {
        _violations.clear();
        _guardians.clear();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        _violations.put(player, new HashMap<>());
        _guardians.put(player, new ArrayList<>());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        _violations.get(player).clear();
        _violations.remove(player);
        _guardians.get(player).clear();
        _guardians.remove(player);
    }

    @EventHandler
    private void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        double distance = player.getLocation().distance(event.getRightClicked().getLocation());

        if (distance > 4) {
            event.setCancelled(true);
        }

        if (distance > 5.5) {
            flag(player, "Reach", CheatSeverity.HIGH);
        } else if (distance > 5) {
            flag(player, "Reach", CheatSeverity.MEDIUM);
        } else if (distance > 4.5) {
            flag(player, "Reach", CheatSeverity.LOW);
        }
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
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

    private Guardian spawnGuardian(Location location) {
        Guardian guardian = (Guardian) location.getWorld().spawnEntity(location, EntityType.GUARDIAN);
        guardian.setCustomName(C.cRed + C.fBold + "HAC");
        guardian.setCustomNameVisible(true);
        guardian.setMetadata("Invulnerable", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("PersistenceRequired", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("Silent", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("NoAI", new FixedMetadataValue(_javaPlugin, 1));
        guardian.setMetadata("CustomName", new FixedMetadataValue(_javaPlugin, guardian.getCustomName()));
        guardian.setMetadata("0Gravity", new FixedMetadataValue(_javaPlugin, 0));
        guardian.teleport(location);

        NBTEditor.set(guardian, 1, "NoAI");
        NBTEditor.set(guardian, 0, "Gravity");

        return guardian;
    }

    final int MAX_POINTS = 180;
    final int RADIUS = 3;

    private Location calculateGuardianOffset(int index, long elapsedTimeMs) {
        // x^2 + y^2 = r^2 - thanks a level maths... (I still googled it)

        final double finalMs = (elapsedTimeMs ^ 2) % 1000;
        final double angle = Math.toRadians(((double) index / MAX_POINTS) * 360d);

        return null;
    }

    public final void ban(Player player, String reason) {
        // TODO: actually ban the player
        animation(player, reason);
    }

    public final void animation(Player player, String reason) {
        Guardian[] guardians = new Guardian[]{
                spawnGuardian(player.getLocation().add(new Vector(3, 6, 0))),
                spawnGuardian(player.getLocation().add(new Vector(-3, 6, 0))),
                spawnGuardian(player.getLocation().add(new Vector(0, 6, 3))),
                spawnGuardian(player.getLocation().add(new Vector(0, 6, -3)))
        };

        for (int i = 0; i < guardians.length; i++) {
//            if ()
        }

        _guardians.get(player).addAll(Arrays.stream(guardians).toList());
    }

    @SuppressWarnings("unused")
    public final void kick(Player player, String reason, CheatSeverity severity, int count) {
        _javaPlugin.getServer().broadcastMessage(F.fMain(this) + F.fItem(player) + " kicked for " + severity.getColor() + reason);
        player.kickPlayer(C.cRed + C.fBold + "You were kicked by Anti-Cheat" + C.fReset + C.cWhite + "\n" + reason);
    }

    public final void alert(Player player, String reason, CheatSeverity severity, int count) {
//        _javaPlugin.getServer().broadcastMessage(F.fMain(this, F.fPlayer(player) + " suspected of " + severity.getColor() + reason));
        _javaPlugin.getServer().broadcastMessage(F.fCheat(player, severity, reason, count, _pluginPortal._serverName));
    }

    public final void flag(final Player player, final String reason, final CheatSeverity severity) {
        final String keyName = reason + ":" + severity.name();

        Map<String, Integer> violations = _violations.get(player);

        if (!violations.containsKey(keyName)) {
            violations.put(keyName, 0);
        }
        int newCount = violations.get(keyName) + 1;
        violations.put(keyName, newCount);

        alert(player, reason, severity, newCount);
        if (newCount == 10) {
            ban(player, reason);
        }
    }

}