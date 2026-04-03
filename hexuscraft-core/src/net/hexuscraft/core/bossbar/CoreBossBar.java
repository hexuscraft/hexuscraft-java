package net.hexuscraft.core.bossbar;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.entity.UtilEntity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

public class CoreBossBar extends MiniPlugin<HexusPlugin>
{

    Map<Player, Set<BossBar>> _bossBarMap;
    Map<Player, Wither> _witherMap;

    public CoreBossBar(HexusPlugin plugin)
    {
        super(plugin, "Boss Bar");
        _bossBarMap = new HashMap<>();
        _witherMap = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        _hexusPlugin.runAsyncTimer(() -> _bossBarMap.forEach((player, bossBars) ->
        {
            if (bossBars.isEmpty())
            {
                return;
            }
            BossBar activeBossBar =
                    bossBars.stream().max(Comparator.comparing(bossBar -> bossBar.weight().get())).orElse(null);

            Wither wither = _witherMap.get(player);
            wither.setCustomName(activeBossBar.message().get());
            wither.getWorld()
                    .getPlayers()
                    .stream()
                    .filter(otherPlayer -> !player.equals(otherPlayer))
                    .forEach(otherPlayer -> ((CraftPlayer) otherPlayer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(
                            wither.getEntityId())));
        }), 0, 1);
    }

    public BossBar registerBossBar(BossBar bossBar)
    {
        if (!_witherMap.containsKey(bossBar.player()))
        {
            Wither wither = bossBar.player().getWorld().spawn(bossBar.player().getLocation(), Wither.class);
            wither.setCanPickupItems(false);
            wither.setFallDistance(0f);
            wither.setFireTicks(0);
            wither.setHealth(300); // Max health allowed is 300
            wither.setMaxHealth(300); // Max health allowed is 300
            wither.setMaximumAir(Integer.MAX_VALUE);
            wither.setMaximumNoDamageTicks(Integer.MAX_VALUE);
            wither.setMetadata("BossBarNPC", new FixedMetadataValue(_hexusPlugin, true));
            wither.setNoDamageTicks(Integer.MAX_VALUE);
            wither.setRemoveWhenFarAway(false);
            wither.setTarget(null);
            wither.setVelocity(new Vector());

            //            We need to add the invisibility via NBT as Entity::addPotionEffect does mot work, and
            //            neither does /effect for that matter! Seriously, try making a wither invisible without
            //            modifying NBT. Why does this have to be so difficult??
            NBTTagCompound nbtInvisibilityEffect = new NBTTagCompound();
            nbtInvisibilityEffect.setByte("Id", (byte) 14);
            nbtInvisibilityEffect.setInt("Duration", Integer.MAX_VALUE);
            nbtInvisibilityEffect.setByte("ShowParticles", (byte) 0);

            NBTTagList nbtActiveEffects = new NBTTagList();
            nbtActiveEffects.add(nbtInvisibilityEffect);

            NBTTagCompound nbt = UtilEntity.getNBTTagCompound(wither);
            nbt.setByte("Invulnerable", (byte) 1);
            nbt.setByte("NoAI", (byte) 1);
            nbt.setByte("Silent", (byte) 1);
            nbt.set("ActiveEffects", nbtActiveEffects);
            UtilEntity.saveNBTTagCompound(wither, nbt);

            _witherMap.put(bossBar.player(), wither);
        }

        Set<BossBar> bossBars;
        if (_bossBarMap.containsKey(bossBar.player()))
        {
            bossBars = _bossBarMap.get(bossBar.player());
        }
        else
        {
            bossBars = new HashSet<>();
            _bossBarMap.put(bossBar.player(), bossBars);
        }
        bossBars.add(bossBar);

        return bossBar;
    }

    public void unregisterBossBar(BossBar bossBar)
    {
        Player player = bossBar.player();

        if (_bossBarMap.containsKey(player))
        {
            Set<BossBar> bossBars = _bossBarMap.get(player);
            bossBars.remove(bossBar);

            if (!bossBars.isEmpty())
            {
                return;
            }
            _bossBarMap.remove(player);
        }

        if (_witherMap.containsKey(player))
        {
            _witherMap.get(player).remove();
            _witherMap.remove(player);
        }
    }

    @EventHandler
    void onEntityTarget(EntityTargetEvent event)
    {
        if (!event.getEntity().hasMetadata("BossBarNPC"))
        {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        if (!_witherMap.containsKey(player))
        {
            return;
        }

        Location location = player.getLocation();
        location.add(player.getEyeLocation().getDirection().multiply(20));
        location.add(player.getVelocity().multiply(10));
        location.setY(Math.clamp(location.getY(), 1, 255));
        location.setYaw(0);
        location.setPitch(0);

        _witherMap.get(player).teleport(location);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (!_bossBarMap.containsKey(player))
        {
            return;
        }
        _bossBarMap.get(player).forEach(this::unregisterBossBar);
    }

}
