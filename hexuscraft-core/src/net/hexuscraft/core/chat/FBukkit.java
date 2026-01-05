package net.hexuscraft.core.chat;

import net.hexuscraft.common.utils.F;
import org.bukkit.Location;

public class FBukkit {

    public static String fItem(final Location location) {
        return F.fItem(Double.toString(location.getX()), Double.toString(location.getY()),
                Double.toString(location.getZ()),
                F.fItem(Float.toString(location.getYaw()), Float.toString(location.getPitch())));
    }

}
