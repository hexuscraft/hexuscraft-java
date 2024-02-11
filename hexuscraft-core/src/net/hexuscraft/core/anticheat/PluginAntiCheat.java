package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.anticheat.command.CommandTestBan;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.entity.NBTEditor;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@SuppressWarnings("unused")
public class PluginAntiCheat extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_TESTBAN
    }

    private PluginPortal _pluginPortal;
    private PluginCommand _pluginCommand;

    private final Map<Player, Map<String, Integer>> _violations;

    private final Map<Player, Set<Guardian>> _guardians;
    private final Set<Player> _inAnimation;

    public PluginAntiCheat(final HexusPlugin plugin) {
        super(plugin, "Anti Cheat");

        _violations = new HashMap<>();
        _guardians = new HashMap<>();
        _inAnimation = new HashSet<>();

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_TESTBAN);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginPortal = (PluginPortal) dependencies.get(PluginPortal.class);
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public final void onEnable() {
        for (Player player : _plugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }

        _pluginCommand.register(new CommandTestBan(this));
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
        _guardians.put(player, new HashSet<>());
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
        guardian.setMetadata("Invulnerable", new FixedMetadataValue(_plugin, 1));
        guardian.setMetadata("PersistenceRequired", new FixedMetadataValue(_plugin, 1));
        guardian.setMetadata("Silent", new FixedMetadataValue(_plugin, 1));
        guardian.setMetadata("NoAI", new FixedMetadataValue(_plugin, 1));
        guardian.setMetadata("CustomName", new FixedMetadataValue(_plugin, guardian.getCustomName()));
        guardian.setMetadata("0Gravity", new FixedMetadataValue(_plugin, 0));
        guardian.teleport(location);

        NBTEditor.set(guardian, 1, "NoAI");
        NBTEditor.set(guardian, 0, "Gravity");

        return guardian;
    }

    final int MAX_POINTS = 180;
    final int RADIUS = 3;

    private Location calculateGuardianOffset(int index, long elapsedTimeMs) {
        final double finalMs = (elapsedTimeMs ^ 2) % 1000;
        final double angle = Math.toRadians(((double) index / MAX_POINTS) * 360d);


        return null;
    }

    public final void ban(Player player, String reason) {
        // TODO: actually ban the player
        animation(player, reason);
    }

    public final void animation(Player player, String reason) {
        if (_inAnimation.contains(player)) return;

        _inAnimation.add(player);

        float oldWalkSpeed = player.getWalkSpeed();
        player.setWalkSpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -10));

        double radius = 4;
        double heightAdj = 8;

        double baseDeg = 18;

        Guardian north = spawnGuardian(player.getLocation().add(0, heightAdj, -radius));
        Guardian east = spawnGuardian(player.getLocation().add(radius, heightAdj, 0));
        Guardian south = spawnGuardian(player.getLocation().add(0, heightAdj, radius));
        Guardian west = spawnGuardian(player.getLocation().add(-radius, heightAdj, 0));
        _guardians.get(player).addAll(List.of(north, east, south, west));
        _guardians.get(player).forEach(guardian -> guardian.setTarget(player));

        _plugin.getServer().getScheduler().runTaskTimer(_plugin, () -> {
            Location northBeforeLook = player.getLocation().add(0, heightAdj, -radius);
            north.teleport(northBeforeLook.setDirection(northBeforeLook.subtract(player.getLocation()).toVector()));

            Location eastBeforeLook = player.getLocation().add(0, heightAdj, -radius);
            east.teleport(eastBeforeLook.setDirection(eastBeforeLook.subtract(player.getLocation()).toVector()));

            Location southBeforeLook = player.getLocation().add(0, heightAdj, -radius);
            south.teleport(southBeforeLook.setDirection(southBeforeLook.subtract(player.getLocation()).toVector()));

            Location westBeforeLook = player.getLocation().add(0, heightAdj, -radius);
            west.teleport(westBeforeLook.setDirection(westBeforeLook.subtract(player.getLocation()).toVector()));
        }, 0, 1L);
    }

    @EventHandler
    public final void on(PlayerMoveEvent event) {
        if (!_inAnimation.contains(event.getPlayer())) return;

        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        event.setCancelled(true);
        event.getPlayer().teleport(from);
    }

    @EventHandler
    public final void on(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        for (Set<Guardian> guardians : _guardians.values()) {
            if (!(entity instanceof Guardian)) continue;
            if (!guardians.contains(entity)) continue;
            event.setCancelled(true);
            break;
        }
    }

    @SuppressWarnings("unused")
    public final void kick(Player player, String reason, CheatSeverity severity, int count) {
        _plugin.getServer().broadcastMessage(F.fMain(this) + F.fItem(player) + " kicked for " + severity.getColor() + reason);
        player.kickPlayer(C.cRed + C.fBold + "You were kicked by Anti-Cheat" + C.fReset + C.cWhite + "\n" + reason);
    }

    public final void alert(Player player, String reason, CheatSeverity severity, int count) {
//        _plugin.getServer().broadcastMessage(F.fMain(this, F.fPlayer(player) + " suspected of " + severity.getColor() + reason));
        _plugin.getServer().broadcastMessage(F.fCheat(player, severity, reason, count, _pluginPortal._serverName));
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