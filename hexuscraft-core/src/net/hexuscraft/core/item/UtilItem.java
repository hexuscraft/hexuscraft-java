package net.hexuscraft.core.item;

import net.hexuscraft.core.chat.C;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public final class UtilItem {

    public static ItemStack createItem(Material material, String displayName, String... lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        List<String> loreList = new ArrayList<>();
        for (String s : lore) {
            loreList.add(C.fReset + C.cGray + s + C.fReset);
        }
        itemMeta.setLore(loreList);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemWool(DyeColor color, String displayName, String... lore) {
        //noinspection deprecation
        ItemStack itemStack = new ItemStack(Material.WOOL, 1, color.getData());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        List<String> loreList = new ArrayList<>();
        for (String s : lore) {
            loreList.add(C.fReset + C.cGray + s + C.fReset);
        }
        itemMeta.setLore(loreList);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createDyeItem(DyeColor color, String displayName, String... lore) {
        //noinspection deprecation
        ItemStack itemStack = new ItemStack(Material.INK_SACK, 1, color.getData());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        List<String> loreList = new ArrayList<>();
        for (String s : lore) {
            loreList.add(C.fReset + C.cGray + s + C.fReset);
        }
        itemMeta.setLore(loreList);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack createItemSkull(String owner, String displayName, String... lore) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwner(owner);
        skullMeta.setDisplayName(displayName);
        skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        List<String> loreList = new ArrayList<>();
        for (String s : lore) {
            loreList.add(C.fReset + C.cGray + s + C.fReset);
        }
        skullMeta.setLore(loreList);

        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }


}
