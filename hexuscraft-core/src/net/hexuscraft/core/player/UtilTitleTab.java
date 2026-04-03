package net.hexuscraft.core.player;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class UtilTitleTab
{

    public static void sendHeaderFooter(Player player, String header, String footer)
    {
        IChatBaseComponent headerComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent footerComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(headerComponent);

        try
        {
            Field field = packet.getClass().getDeclaredField("b");
            field.setAccessible(true);
            field.set(packet, footerComponent);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendActionText(Player player, String text)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(text),
                (byte) 2));
    }

    public static void sendTitle(Player player, String text, int fadeInTicks, int stayInTicks, int fadeOutTicks)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                new ChatComponentText(text),
                fadeInTicks,
                stayInTicks,
                fadeOutTicks));
    }

    public static void sendSubtitle(Player player, String text, int fadeInTicks, int stayInTicks, int fadeOutTicks)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                new ChatComponentText(text),
                fadeInTicks,
                stayInTicks,
                fadeOutTicks));
    }

    public static void sendTimes(Player player, int fadeInTicks, int stayInTicks, int fadeOutTicks)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES,
                null,
                fadeInTicks,
                stayInTicks,
                fadeOutTicks));
    }

    public static void sendReset(Player player)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET,
                null));
    }

    public static void sendClear(Player player)
    {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR,
                null));
    }

}
