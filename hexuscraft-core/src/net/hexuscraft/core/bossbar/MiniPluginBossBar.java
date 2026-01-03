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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public final class MiniPluginBossBar extends MiniPlugin<HexusPlugin> {

    final Map<Player, Set<BossBar>> _bossBarMap;

    public MiniPluginBossBar(final HexusPlugin plugin) {
        super(plugin, "Boss Bar");
        _bossBarMap = new HashMap<>();
    }

    @Override
    public void onEnable() {
        _hexusPlugin.runSyncTimer(() -> {
            _bossBarMap.forEach((player, bossBars) -> {
                final BossBar activeBossBar =
                        bossBars.stream().max(Comparator.comparing(bossBar -> bossBar._weight.get())).orElse(null);
                if (activeBossBar == null) return;

                final Location location = player.getLocation();
                location.add(player.getEyeLocation().getDirection().multiply(40));
                location.setY(Math.clamp(location.getY(), 1, 255));

                activeBossBar._entity.setHealth(Math.clamp(activeBossBar._progress.get() * 100, 1, 100));
                activeBossBar._entity.setCustomName(activeBossBar._message.get());
                activeBossBar._entity.teleport(location);
            });
        }, 0, 1);
    }

    public BossBar registerBossBar(final Player player) {
        final BossBar bossBar =
                new BossBar(player, (Wither) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER));

        bossBar._entity.getWorld().getPlayers().stream().filter(otherPlayer -> !player.equals(otherPlayer)).forEach(
                otherPlayer -> ((CraftPlayer) otherPlayer).getHandle().playerConnection.sendPacket(
                        new PacketPlayOutEntityDestroy(bossBar._entity.getEntityId())));

        bossBar._entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, false, false), true);
        bossBar._entity.setMaxHealth(100);
        bossBar._entity.setHealth(100);
        bossBar._entity.setCustomNameVisible(true);
        bossBar._entity.setRemoveWhenFarAway(false);
        bossBar._entity.setMetadata("BossBarNPC", new FixedMetadataValue(_hexusPlugin, true));

        final NBTTagCompound nbtTagCompound = UtilEntity.getNBTTagCompound(bossBar._entity);
        nbtTagCompound.setByte("NoAI", (byte) 1);
        nbtTagCompound.setByte("Silent", (byte) 1);
        UtilEntity.saveNBTTagCompound(bossBar._entity, nbtTagCompound);

        if (!_bossBarMap.containsKey(bossBar._player)) _bossBarMap.put(bossBar._player, new HashSet<>());
        _bossBarMap.get(bossBar._player).add(bossBar);

        return bossBar;
    }

    public void unregisterBossBar(final BossBar bossBar) {
        bossBar._entity.remove();

        final Set<BossBar> bossBarSet = _bossBarMap.get(bossBar._player);
        bossBarSet.remove(bossBar);
        if (bossBarSet.isEmpty()) _bossBarMap.remove(bossBar._player);
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
    private void onPlayerJoin(final PlayerJoinEvent event) {
        _bossBarMap.forEach((player, bossBars) -> {
            if (player.equals(event.getPlayer())) return;
            bossBars.forEach(bossBar -> ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(
                    new PacketPlayOutEntityDestroy(bossBar._entity.getEntityId())));
        });
    }

    @EventHandler
    private void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        _bossBarMap.forEach((player, bossBars) -> {
            if (player.equals(event.getPlayer())) return;
            bossBars.forEach(bossBar -> ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.sendPacket(
                    new PacketPlayOutEntityDestroy(bossBar._entity.getEntityId())));
        });
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (!_bossBarMap.containsKey(player)) return;
        _bossBarMap.get(player).forEach(this::unregisterBossBar);
    }

}
