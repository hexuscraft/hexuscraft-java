package net.hexuscraft.core.entity;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public final class UtilEntity {

	// NBTBase.a = ["END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"]

	public static NBTTagCompound getNBTTagCompound(Entity entity) {
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		((CraftEntity) entity).getHandle().c(nbtTagCompound);
		return nbtTagCompound;
	}

	public static void saveNBTTagCompound(Entity entity, NBTTagCompound nbtTagCompound) {
		((CraftEntity) entity).getHandle().f(nbtTagCompound);
	}

	public static class NBTType {
		public static final int END = 0;
		public static final int BYTE = 1;
		public static final int SHORT = 2;
		public static final int INT = 3;
		public static final int LONG = 4;
		public static final int FLOAT = 5;
		public static final int DOUBLE = 6;
		public static final int BYTE_ARRAY = 7;
		public static final int STRING = 8;
		public static final int LIST = 9;
		public static final int COMPOUND = 10;
		public static final int INT_ARRAY = 11;
	}

}
