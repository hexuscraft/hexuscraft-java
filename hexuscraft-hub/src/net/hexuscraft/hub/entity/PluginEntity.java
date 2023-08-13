package net.hexuscraft.hub.entity;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.hub.entity.command.CommandEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class PluginEntity extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_ENTITY,
        COMMAND_ENTITY_LIST,
        COMMAND_ENTITY_PURGE,
        COMMAND_ENTITY_REFRESH
    }

    PluginCommand _pluginCommand;

    public PluginEntity(JavaPlugin javaPlugin) {
        super(javaPlugin, "Entity");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandEntity(this));

//        _javaPlugin.getServer().getWorlds().forEach(this::refreshNPCs);
    }

    @Override
    public void onDisable() {
        _javaPlugin.getServer().getWorlds().forEach(this::removeNPCs);
    }

    void createEntity(World world, double x, double y, double z, float yaw, float pitch, String[] data) {
        log(String.join(", ", new String[]{world.toString(), Double.toString(x), Double.toString(y), Double.toString(z), Float.toString(yaw), Float.toString(pitch), String.join(",", data)}));
        Location location = new Location(world, x, y, z, yaw, pitch);
        //noinspection ReassignedVariable
        Entity entity = null;
        if (data[0].equals("REWARDS")) {
//            Creeper creeper = (Creeper) world.spawnEntity(location, EntityType.CREEPER);
            Creeper creeper = world.spawn(location, Creeper.class);
            creeper.setPowered(true);
            creeper.setCustomName(C.cGreen + C.fBold + "Server Rewards");
            creeper.setCustomNameVisible(true);

            entity = creeper;
        }
        if (data[0].equals("GAME")) {
            if (data[1].equals("SURVIVAL_GAMES")) {
                Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
                zombie.setCustomName(C.cGreen + C.fBold + "Survival Games");
                zombie.setCustomNameVisible(true);

                PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                slow.apply(zombie);

                EntityEquipment equipment = zombie.getEquipment();
                equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                equipment.setItemInHand(new ItemStack(Material.IRON_SWORD));

                entity = zombie;
            }
            if (data[1].equals("TOWER_BATTLES")) {
                PigZombie pigZombie = (PigZombie) world.spawnEntity(location, EntityType.PIG_ZOMBIE);
                pigZombie.setCustomName(C.cGreen + C.fBold + "Tower Battles");
                pigZombie.setCustomNameVisible(true);

                PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                slow.apply(pigZombie);

                EntityEquipment equipment = pigZombie.getEquipment();
                equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                equipment.setItemInHand(new ItemStack(Material.DIRT));

                entity = pigZombie;
            }
            if (data[1].equals("SKYWARS")) {
                Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
                skeleton.setCustomName(C.cGreen + C.fBold + "Skywars");
                skeleton.setCustomNameVisible(true);

                PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 200, true, false);
                slow.apply(skeleton);

                EntityEquipment equipment = skeleton.getEquipment();
                equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                equipment.setItemInHand(new ItemStack(Material.BOW));

                entity = skeleton;
            }
        }
        if (entity == null) {
            log("Attempted to create entity with unknown data types: " + String.join(",", data));
            return;
        }

        entity.setMetadata("NPC", new FixedMetadataValue(_javaPlugin, data[0]));
        if (data.length > 1) {
            entity.setMetadata(data[0], new FixedMetadataValue(_javaPlugin, data[1]));
        }

        entity.teleport(location);
    }

    void removeNPCs(World world) {
        world.getEntities().forEach(entity -> {
            if (entity.getMetadata("NPC").isEmpty()) {
                return;
            }
            entity.remove();
        });
    }

    public void refreshNPCs(World world) {
        removeNPCs(world);

        String npcFileContent;
        try {
            npcFileContent = new Scanner(Path.of(world.getWorldFolder().getPath(), "npcs.dat").toFile()).useDelimiter("\\Z").next();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Arrays.asList(npcFileContent.split("\\n")).forEach(s -> {
//            log(s);
            String[] npc = s.split(",");
            double x = Double.parseDouble(npc[0]);
            double y = Double.parseDouble(npc[1]);
            double z = Double.parseDouble(npc[2]);
            float yaw = Float.parseFloat(npc[3]);
            float pitch = Float.parseFloat(npc[4]);

            String[] data = Arrays.copyOfRange(npc, 5, npc.length);
            createEntity(world, x, y, z, yaw, pitch, data);
        });
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
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

}
