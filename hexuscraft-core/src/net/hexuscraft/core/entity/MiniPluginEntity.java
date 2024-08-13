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
        COMMAND_ENTITY,
        COMMAND_ENTITY_LIST,
        COMMAND_ENTITY_PURGE,
        COMMAND_ENTITY_REFRESH
    }

    private MiniPluginCommand _pluginCommand;

    public MiniPluginEntity(final HexusPlugin plugin) {
        super(plugin, "Entity");

        PermissionGroup.BUILDER._permissions.addAll(List.of(
                PERM.COMMAND_ENTITY,
                PERM.COMMAND_ENTITY_LIST
        ));

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

        _hexusPlugin.getServer().getWorlds().forEach(this::refreshNPCs);

        final Map<Entity, Location> entityLocationMap = new HashMap<>();
        final Server server = _hexusPlugin.getServer();
        server.getScheduler().runTaskTimer(_hexusPlugin, () -> server.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            final Location from = entityLocationMap.get(entity);
            final Location to = entity.getLocation();

            if (!to.equals(from)) {
                final EntityMoveEvent event = new EntityMoveEvent(entity, from, to);
                _hexusPlugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    entity.teleport(event.getFrom());
                    return;
                }
            }

            entityLocationMap.put(entity, to);
        })), 0, 1L);
    }

    @Override
    public void onDisable() {
        _hexusPlugin.getServer().getWorlds().forEach(this::removeNPCs);
    }

    public void createEntity(final World world, final double x, final double y, final double z,
                             final float yaw, final float pitch, final String[] data) {
//        log(String.join(", ", new String[]{world.toString(), Double.toString(x), Double.toString(y), Double.toString(z), Float.toString(yaw), Float.toString(pitch), String.join(":", data)}));
        final Location location = new Location(world, x, y, z, yaw, pitch);

        //noinspection ReassignedVariable
        Entity entity = null;
        switch (data[0]) {
            case "REWARDS" -> {
                final Creeper creeper = world.spawn(location, Creeper.class);
                creeper.setPowered(true);
                creeper.setCustomName(C.cGreen + C.fBold + "Server Rewards");
                creeper.setCustomNameVisible(true);

                entity = creeper;
            }
            case "GAME" -> {
                switch (data[1]) {
                    case "CLANS" -> {
                        final Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
                        skeleton.setCustomName(C.cGreen + C.fBold + "Clans");
                        skeleton.setCustomNameVisible(true);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(skeleton);

                        final EntityEquipment equipment = skeleton.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.BOW));

                        entity = skeleton;
                    }
                    case "SURVIVAL_GAMES" -> {
                        final Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
                        zombie.setBaby(false);
                        zombie.setVillager(false);
                        zombie.setCustomName(C.cGreen + C.fBold + "Survival Games");
                        zombie.setCustomNameVisible(true);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(zombie);

                        final EntityEquipment equipment = zombie.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.IRON_SWORD));

                        entity = zombie;
                    }
                    case "TOWER_BATTLES" -> {
                        final PigZombie pigZombie = (PigZombie) world.spawnEntity(location, EntityType.PIG_ZOMBIE);
                        pigZombie.setCustomName(C.cGreen + C.fBold + "Tower Battles");
                        pigZombie.setCustomNameVisible(true);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(pigZombie);

                        final EntityEquipment equipment = pigZombie.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.DIRT));

                        entity = pigZombie;
                    }
                    case "SKYWARS" -> {
                        final Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
                        skeleton.setCustomName(C.cGreen + C.fBold + "Skywars");
                        skeleton.setCustomNameVisible(true);

                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                        slow.apply(skeleton);

                        final EntityEquipment equipment = skeleton.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.BOW));

                        entity = skeleton;
                    }
                }
            }
        }

        if (entity == null) {
            log("Attempted to create entity with unknown data types: " + String.join(",", data));
            return;
        }

        entity.setMetadata("NPC", new FixedMetadataValue(_hexusPlugin, data[0]));
        for (int i = 1; i < data.length; i++) {
            entity.setMetadata(data[i - 1], new FixedMetadataValue(_hexusPlugin, data[i]));
        }

        entity.teleport(location);
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
            //noinspection SpellCheckingInspection
            final Scanner scanner = new Scanner(Path.of(world.getWorldFolder().getPath(), "_npcs.dat").toFile());
            while (scanner.hasNextLine()) {
                npcStrings.add(scanner.nextLine());
            }
        } catch (FileNotFoundException ex) {
            log("Could not locate _npcs.dat in world '" + world.getName() + "'");
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
        // TODO
        return new Entity[0];
    }

    @SuppressWarnings("SameReturnValue")
    public int purge() {
        // TODO
        return 0;
    }

    // TODO: Fix this - it keeps crashing :(
//    @EventHandler
//    public void onEntityMove(final EntityMoveEvent event) {
//        if (!event.getEntity().hasMetadata("NPC")) return;
//        if (!event.isHorizontal(false)) return;
//        event.setCancelled(true);
//    }

}
