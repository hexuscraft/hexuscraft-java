package net.hexuscraft.core.entity;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

public final class UtilEntity {

    public static NBTTagCompound getTagCompound(final CraftEntity entity) {
        return entity.getHandle().getNBTTag();
    }


}
