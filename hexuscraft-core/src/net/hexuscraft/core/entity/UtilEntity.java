package net.hexuscraft.core.entity;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public final class UtilEntity {

    public static NBTTagCompound getNBTTagCompound(final Entity entity) {
        final NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftEntity) entity).getHandle().c(nbtTagCompound);
        return nbtTagCompound;
    }

    public static void saveNBTTagCompound(final Entity entity, final NBTTagCompound nbtTagCompound) {
        ((CraftEntity) entity).getHandle().f(nbtTagCompound);
    }

}
