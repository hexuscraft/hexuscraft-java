package net.hexuscraft.core.item;

import net.hexuscraft.common.chat.F;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Predicate;

public final class ItemSearch {

    public static boolean isMaterialAnItem(final Material material) {
        final Inventory inventory = Bukkit.createInventory(null, 9);
        inventory.addItem(new ItemStack(material));
        return inventory.contains(material);
    }

    public static Material[] itemSearch(final String searchName) {
        if (searchName.equals("*")) return Material.values();

        final Material[] matches = Arrays.stream(Material.values()).filter(ItemSearch::isMaterialAnItem)
                .filter(material -> material.name().toLowerCase().contains(searchName.toLowerCase()))
                .toArray(Material[]::new);

        for (final Material match : matches)
            if (match.name().equalsIgnoreCase(searchName)) return new Material[]{match};

        try {
            //noinspection deprecation
            return new Material[]{Material.getMaterial(Integer.parseInt(searchName))};
        } catch (final Exception ignored) {
        }

        return matches;
    }

    public static Material[] itemSearch(final String searchName, final CommandSender sender,
                                        final Predicate<Material[]> shouldSendMatches) {
        final Material[] matches = itemSearch(searchName);
        if (shouldSendMatches.test(matches)) sender.sendMessage(F.fMain("Item Search",
                F.fMatches(Arrays.stream(matches).map(Material::name).toArray(String[]::new), searchName)));
        return matches;
    }

}
