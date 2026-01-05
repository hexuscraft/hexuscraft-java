package net.hexuscraft.core.bossbar;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.entity.UtilEntity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class MiniPluginBossBar extends MiniPlugin<HexusPlugin> {

    final Map<Player, Set<BossBar>> _bossBarMap;
    final Map<Player, Wither> _witherMap;

    public MiniPluginBossBar(final HexusPlugin plugin) {
        super(plugin, "Boss Bar");
        _bossBarMap = new HashMap<>();
        _witherMap = new HashMap<>();
    }

    @Override
    public void onEnable() {
        _hexusPlugin.runAsyncTimer(() -> {
            _bossBarMap.forEach((player, bossBars) -> {
                final BossBar activeBossBar =
                        bossBars.stream().max(Comparator.comparing(bossBar -> bossBar._weight.get())).orElse(null);
                if (activeBossBar == null) return;

                final Location location = player.getLocation();
                location.add(player.getEyeLocation().getDirection().multiply(10));
                location.setY(Math.clamp(location.getY(), 1, 255));
                location.setYaw(0);
                location.setPitch(0);

                final Wither wither = _witherMap.get(player);
                wither.teleport(location);
                wither.setHealth(Math.clamp(activeBossBar._progress.get() * 100, 1, 100));
                wither.setCustomName(activeBossBar._message.get());

                wither.getWorld().getPlayers().stream().filter(otherPlayer -> !player.equals(otherPlayer)).forEach(
                        otherPlayer -> ((CraftPlayer) otherPlayer).getHandle().playerConnection.sendPacket(
                                new PacketPlayOutEntityDestroy(wither.getEntityId())));
            });
        }, 0, 1);
    }

    public BossBar registerBossBar(final BossBar bossBar) {
        if (!_witherMap.containsKey(bossBar._player)) {
            final Wither wither =
                    (Wither) bossBar._player.getWorld().spawnEntity(bossBar._player.getLocation(), EntityType.WITHER);
            wither.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false),
                    true);
            wither.setMaxHealth(100);
            wither.setHealth(100);
            wither.setCustomNameVisible(false);
            wither.setRemoveWhenFarAway(false);
            wither.setTarget(null);
            wither.setMaximumNoDamageTicks(Integer.MAX_VALUE);
            wither.setNoDamageTicks(Integer.MAX_VALUE);
            wither.setMetadata("BossBarNPC", new FixedMetadataValue(_hexusPlugin, true));

            final NBTTagCompound nbtTagCompound = UtilEntity.getNBTTagCompound(wither);
            nbtTagCompound.setByte("NoAI", (byte) 1);
            nbtTagCompound.setByte("Silent", (byte) 1);
            UtilEntity.saveNBTTagCompound(wither, nbtTagCompound);

            _witherMap.put(bossBar._player, wither);
        }

        final Set<BossBar> bossBars;
        if (_bossBarMap.containsKey(bossBar._player)) bossBars = _bossBarMap.get(bossBar._player);
        else {
            bossBars = new HashSet<>();
            _bossBarMap.put(bossBar._player, bossBars);
        }
        bossBars.add(bossBar);

        return bossBar;
    }

    public void unregisterBossBar(final BossBar bossBar) {
        final Set<BossBar> bossBarSet = _bossBarMap.get(bossBar._player);
        bossBarSet.remove(bossBar);

        if (bossBarSet.isEmpty()) {
            _bossBarMap.remove(bossBar._player);

            if (_witherMap.containsKey(bossBar._player)) {
                _witherMap.get(bossBar._player).remove();
                _witherMap.remove(bossBar._player);
            }
        }
    }

    @EventHandler
    private void onEntityTarget(final EntityTargetEvent event) {
        if (!event.getEntity().hasMetadata("BossBarNPC")) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!event.getEntity().hasMetadata("BossBarNPC")) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (!_bossBarMap.containsKey(player)) return;
        _bossBarMap.get(player).forEach(this::unregisterBossBar);
    }

}
