package net.hexuscraft.core.entity;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class UtilEntity
{

    public static NBTTagCompound getNBTTagCompound(Entity entity)
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ((CraftEntity) entity).getHandle().c(nbtTagCompound);
        return nbtTagCompound;
    }

    public static void saveNBTTagCompound(Entity entity, NBTTagCompound nbtTagCompound)
    {
        ((CraftEntity) entity).getHandle().f(nbtTagCompound);
    }

}
