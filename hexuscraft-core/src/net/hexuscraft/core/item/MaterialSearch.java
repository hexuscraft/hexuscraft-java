package net.hexuscraft.core.item;

import net.hexuscraft.core.chat.F;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaterialSearch {

    public static Material[] materialSearch(final String searchName) {
        if (searchName.equals("*")) {
            return Material.values();
        }

        List<Material> matches = new ArrayList<>();
        for (Material target : Material.values()) {
            String targetName = target.name();
            if (targetName.equals(searchName)) {
                return new Material[]{target};
            }
            if (!targetName.contains(searchName)) {
                continue;
            }
            matches.add(target);
        }
        return matches.toArray(Material[]::new);
    }

    public static Material[] materialSearch(final String searchName, final CommandSender sender) {
        final Material[] matches = materialSearch(searchName);

        sender.sendMessage(F.fMain("Material Search") + F.fItem(matches.length + (matches.length == 1 ? " Match" : " Matches")) + " for " + F.fItem(searchName) + (matches.length == 0 ? "." : ":\n" +
                F.fMain("") + F.fList(Arrays.stream(matches).map(Material::name).toArray(String[]::new))));

        return matches;
    }

}
