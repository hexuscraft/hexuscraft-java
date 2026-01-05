package net.hexuscraft.core.npc;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.entity.UtilEntity;
import net.hexuscraft.core.npc.command.CommandNpc;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

public final class MiniPluginNpc extends MiniPlugin<HexusPlugin> {

    private MiniPluginCommand _pluginCommand;

    public MiniPluginNpc(final HexusPlugin plugin) {
        super(plugin, "NPC");

        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_ENTITY);
        PermissionGroup.BUILD_TEAM._permissions.add(PERM.COMMAND_ENTITY_LIST);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ENTITY_REFRESH);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_ENTITY_PURGE);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandNpc(this));

        final Server server = _hexusPlugin.getServer();
        server.getWorlds().forEach(this::refreshNPCs);
    }

    @Override
    public void onDisable() {
        _hexusPlugin.getServer().getWorlds().forEach(this::removeNPCs);
    }

    public void createEntity(final World world, final double x, final double y, final double z, final float yaw,
                             final float pitch, final String[] data) {
        final Location location = new Location(world, x, y, z, yaw, pitch);

        final List<Entity> spawnedEntities = new ArrayList<>();
        switch (data[0]) {
            case "REWARDS" -> {
                final Creeper creeper = world.spawn(location, Creeper.class);
                spawnedEntities.add(creeper);
                creeper.setPowered(true);
                creeper.teleport(location);

                final NBTTagCompound creperNBT = UtilEntity.getNBTTagCompound(creeper);
                creperNBT.setByte("NoAI", (byte) 1);
                creperNBT.setByte("Silent", (byte) 1);
                creperNBT.setByte("Invulnerable", (byte) 1);
                UtilEntity.saveNBTTagCompound(creeper, creperNBT);

                final ArmorStand armorStand = world.spawn(location, ArmorStand.class);
                spawnedEntities.add(armorStand);
                armorStand.setCustomName(C.cGreen + C.fBold + "Server Rewards");
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setMarker(true);
                armorStand.teleport(creeper.getEyeLocation().add(new Vector(0, 0.25, 0)));

                final NBTTagCompound armorStandNBT = UtilEntity.getNBTTagCompound(armorStand);
                armorStandNBT.setByte("Invisible", (byte) 1);
                armorStandNBT.setByte("NoGravity", (byte) 1);
                armorStandNBT.setByte("Marker", (byte) 1);
                UtilEntity.saveNBTTagCompound(armorStand, armorStandNBT);
            }
            case "GAME" -> {
                switch (data[1]) {
                    case "CLANS" -> {
                        final Skeleton skeleton = world.spawn(location, Skeleton.class);
                        spawnedEntities.add(skeleton);
                        skeleton.teleport(location);

                        final NBTTagCompound skeletonNBT = UtilEntity.getNBTTagCompound(skeleton);
                        skeletonNBT.setByte("NoAI", (byte) 1);
                        skeletonNBT.setByte("Silent", (byte) 1);
                        skeletonNBT.setByte("Invulnerable", (byte) 1);
                        UtilEntity.saveNBTTagCompound(skeleton, skeletonNBT);

                        final EntityEquipment equipment = skeleton.getEquipment();
                        equipment.setHelmet(new ItemStack(Material.STONE_BUTTON));
                        equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                        equipment.setItemInHand(new ItemStack(Material.BED));

                        final ArmorStand armorStand = world.spawn(location, ArmorStand.class);
                        spawnedEntities.add(armorStand);
                        armorStand.setCustomName(C.cGreen + C.fBold + "Clans");
                        armorStand.setCustomNameVisible(true);
                        armorStand.setGravity(false);
                        armorStand.setMarker(true);
                        armorStand.teleport(skeleton.getEyeLocation().add(new Vector(0, 0.25, 0)));

                        final NBTTagCompound armorStandNBT = UtilEntity.getNBTTagCompound(armorStand);
                        armorStandNBT.setByte("Invisible", (byte) 1);
                        armorStandNBT.setByte("NoGravity", (byte) 1);
                        armorStandNBT.setByte("Marker", (byte) 1);
                        UtilEntity.saveNBTTagCompound(armorStand, armorStandNBT);
                    }
                }
            }
        }

        if (spawnedEntities.isEmpty()) {
            logInfo("Attempted to create npc with unknown punish types: " + String.join(",", data));
            return;
        }

        spawnedEntities.forEach(entity -> entity.setMetadata("NPC", new FixedMetadataValue(_hexusPlugin, data)));
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
        _hexusPlugin.getServer().getWorlds().forEach(world -> npcs.addAll(
                world.getEntities().stream().filter(entity -> entity.hasMetadata("NPC")).toList()));
        return npcs.toArray(Entity[]::new);
    }

    public enum PERM implements IPermission {
        COMMAND_ENTITY, COMMAND_ENTITY_LIST, COMMAND_ENTITY_PURGE, COMMAND_ENTITY_REFRESH
    }

}
