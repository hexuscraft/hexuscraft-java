package net.hexuscraft.core.item;

import net.hexuscraft.common.utils.C;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class UtilItem
{

    private static ItemStack applyMeta(ItemStack itemStack, String displayName, String... lore)
    {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        List<String> loreList = new ArrayList<>();
        for (String s : lore)
        {
            loreList.add(C.fReset + C.cGray + s + C.fReset);
        }
        itemMeta.setLore(loreList);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack create(Material material, String displayName, String... lore)
    {
        ItemStack itemStack = new ItemStack(material);
        applyMeta(itemStack, displayName, lore);
        return itemStack;
    }

    public static ItemStack createWithData(Material material, byte data, String displayName, String... lore)
    {
        ItemStack itemStack = new ItemStack(material, 1, data);
        applyMeta(itemStack, displayName, lore);
        return itemStack;
    }

    public static ItemStack createWool(DyeColor color, String displayName, String... lore)
    {
        //noinspection deprecation
        return createWithData(Material.WOOL, color.getData(), displayName, lore);
    }

    public static ItemStack createPlayerSkull(String owner, String displayName, String... lore)
    {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwner(owner);
        itemStack.setItemMeta(skullMeta);
        applyMeta(itemStack, displayName, lore);
        return itemStack;
    }

}
