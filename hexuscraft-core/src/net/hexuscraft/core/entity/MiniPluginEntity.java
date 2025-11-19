package net.hexuscraft.core.entity;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.entity.command.CommandEntity;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

public final class MiniPluginEntity extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_ENTITY, COMMAND_ENTITY_LIST, COMMAND_ENTITY_PURGE, COMMAND_ENTITY_REFRESH
    }

    private MiniPluginCommand _pluginCommand;
    private final Map<Entity, Location> _entityLocationMap = new HashMap<>();

    public MiniPluginEntity(final HexusPlugin plugin) {
        super(plugin, "Entity");

        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_ENTITY);
        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_ENTITY_LIST);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ENTITY_REFRESH);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ENTITY_PURGE);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandEntity(this));

        final Server server = _hexusPlugin.getServer();
        server.getWorlds().forEach(this::refreshNPCs);
        server.getScheduler().runTaskTimer(_hexusPlugin, () -> server.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            final EntityMoveEvent event = new EntityMoveEvent(entity, _entityLocationMap.get(entity), entity.getLocation());
            _entityLocationMap.put(entity, event._to);
            if (!event.isAny()) return;

            _hexusPlugin.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) return;

            entity.teleport(event._from);
            _entityLocationMap.put(entity, event._from);
        })), 0, 1L);
    }

    @Override
    public void onDisable() {
        _hexusPlugin.getServer().getWorlds().forEach(this::removeNPCs);
    }

    public void createEntity(final World world, final double x, final double y, final double z, final float yaw, final float pitch, final String[] data) {
        final Location location = new Location(world, x, y, z, yaw, pitch);

        final List<Entity> spawnedEntities = new ArrayList<>();
        switch (data[0]) {
            case "REWARDS" -> {
                final Creeper creeper = world.spawn(location, Creeper.class);
                spawnedEntities.add(creeper);
                creeper.setPowered(true);
                creeper.teleport(location);
                creeper.setMetadata("Invulnerable", new FixedMetadataValue(_hexusPlugin, 1));

                final Silverfish silverfish = world.spawn(location, Silverfish.class);
                spawnedEntities.add(silverfish);
                creeper.setPassenger(silverfish);
                silverfish.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, true, false));
                silverfish.setMetadata("NoAI", new FixedMetadataValue(_hexusPlugin, 1));
                silverfish.setMetadata("Invulnerable", new FixedMetadataValue(_hexusPlugin, 1));

                final ArmorStand armorStand = world.spawn(location, ArmorStand.class);
                spawnedEntities.add(armorStand);
                silverfish.setPassenger(armorStand);
                armorStand.setCustomName(C.cGreen + C.fBold + "Server Rewards");
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setMarker(true);
                armorStand.setMetadata("Invisible", new FixedMetadataValue(_hexusPlugin, 1));
            }
            case "GAME" -> {
                switch (data[1]) {
                    case "CLANS" -> {
                        final Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
                        skeleton.setCustomName(C.cGreen + C.fBold + "Clans");
                        skeleton.setCustomNameVisible(true);
                        skeleton.teleport(location);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(skeleton);

                        final EntityEquipment equipment = skeleton.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.BOW));

                        spawnedEntities.add(skeleton);
                    }
                    case "SURVIVAL_GAMES" -> {
                        final Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
                        zombie.setBaby(false);
                        zombie.setVillager(false);
                        zombie.setCustomName(C.cGreen + C.fBold + "Survival Games");
                        zombie.setCustomNameVisible(true);
                        zombie.teleport(location);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(zombie);

                        final EntityEquipment equipment = zombie.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.IRON_SWORD));

                        spawnedEntities.add(zombie);
                    }
                    case "TOWER_BATTLES" -> {
                        final PigZombie pigZombie = (PigZombie) world.spawnEntity(location, EntityType.PIG_ZOMBIE);
                        pigZombie.setCustomName(C.cGreen + C.fBold + "Tower Battles");
                        pigZombie.setCustomNameVisible(true);
                        pigZombie.teleport(location);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(pigZombie);

                        final EntityEquipment equipment = pigZombie.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.DIRT));

                        spawnedEntities.add(pigZombie);
                    }
                    case "SKYWARS" -> {
                        final Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
                        skeleton.setCustomName(C.cGreen + C.fBold + "Skywars");
                        skeleton.setCustomNameVisible(true);
                        skeleton.teleport(location);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(skeleton);

                        final EntityEquipment equipment = skeleton.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.BOW));

                        spawnedEntities.add(skeleton);
                    }
                }
            }
        }

        if (spawnedEntities.isEmpty()) {
            logInfo("Attempted to create entity with unknown data types: " + String.join(",", data));
            return;
        }

        spawnedEntities.forEach(entity -> {
            entity.setMetadata("NPC", new FixedMetadataValue(_hexusPlugin, data[0]));
            for (int i = 1; i < data.length; i++) {
                entity.setMetadata(data[i - 1], new FixedMetadataValue(_hexusPlugin, data[i]));
            }
        });
    }

    public void removeNPCs(final World world) {
        world.getEntities().forEach(entity -> {
            if (entity.getMetadata("NPC").isEmpty()) return;
            entity.remove();
        });
    }

    public void refreshNPCs(final World world) {
        removeNPCs(world);

        final List<String> npcStrings = new ArrayList<>();

        try {
            final Scanner scanner = new Scanner(Path.of(world.getWorldFolder().getPath(), "_npcs.dat").toFile());
            while (scanner.hasNextLine()) {
                npcStrings.add(scanner.nextLine());
            }
        } catch (FileNotFoundException ex) {
            logInfo("Could not locate _npcs.dat in world '" + world.getName() + "'");
        }

        npcStrings.forEach(s -> {
            final String[] npc = s.split(",");
            final double x = Double.parseDouble(npc[0]);
            final double y = Double.parseDouble(npc[1]);
            final double z = Double.parseDouble(npc[2]);
            final float yaw = Float.parseFloat(npc[3]);
            final float pitch = Float.parseFloat(npc[4]);

            final String[] data = Arrays.copyOfRange(npc, 5, npc.length);
            createEntity(world, x, y, z, yaw, pitch, data);
        });
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        refreshNPCs(event.getWorld());
    }

    public Entity[] list() {
        final List<Entity> npcs = new ArrayList<>();
        _hexusPlugin.getServer().getWorlds().forEach(world -> npcs.addAll(world.getEntities().stream().filter(entity -> entity.hasMetadata("NPC")).toList()));
        return npcs.toArray(Entity[]::new);
    }

    @SuppressWarnings("SameReturnValue")
    public int purge() {
        // TODO
        return 0;
    }

    @EventHandler
    public void onEntityMove(final EntityMoveEvent event) {
        if (!event.getEntity().hasMetadata("NPC")) return;
        if (!event.isHorizontal(false)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(final EntityTeleportEvent event) {
        _entityLocationMap.put(event.getEntity(), event.getTo());
    }

}
