package net.hexuscraft.core.item.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.PluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandGive extends BaseCommand {

    public CommandGive(PluginItem itemCenter) {
        super(itemCenter, "give", "<Players> <Item> [Amount] [Enchantment:Level,...]", "Give items to players.", Set.of("g", "item", "i"), PluginItem.PERM.COMMAND_GIVE);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        Player[] targets;

        switch (args[0]) {
            case "." -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(F.fMain(this) + "Only players can use this selector.");
                    return;
                }
                targets = new Player[]{(Player) sender};
            }
            case "*" -> targets = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().toArray(new Player[0]);

            case "**" -> {
                targets = (Player[]) _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().filter(player -> !player.getName().equals(sender.getName())).toArray();
            }
            default -> {
                targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0]);
            }
        }

        if (targets.length == 0) {
            sender.sendMessage(F.fMain(this) + "Found no players with name " + F.fEntity(args[0]) + ".");
            return;
        }

        int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;

        HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (args.length > 3) {
            for (String enchantmentTableRaw : args[3].split(",")) {
                String[] enchantmentTable = enchantmentTableRaw.split(":", 2);
                Enchantment enchantment = Enchantment.getByName(enchantmentTable[0]);
                int enchantmentLevel = enchantmentTable.length > 1 ? Integer.parseInt(enchantmentTable[2]) : 1;
                enchantmentMap.put(enchantment, enchantmentLevel);
            }
        }

        for (String materialSearchName : args[1].split(",")) {
            Material[] targetMaterials = materialSearch(materialSearchName);
            if (targetMaterials.length != 1) {
                StringBuilder builder = new StringBuilder();
                builder.append(F.fMain("Material Search")).append(F.fItem(targetMaterials.length + " Matches")).append(" for ").append(F.fItem(materialSearchName)).append(".");
                if (targetMaterials.length > 1) {
                    builder.append(F.fMain()).append(F.fList((String[]) Arrays.stream(targetMaterials).map(Material::name).toArray()));
                }
                sender.sendMessage(builder.toString());
                continue;
            }

            ItemStack stack = new ItemStack(targetMaterials[0]);
            stack.setAmount(amount);

            enchantmentMap.forEach(stack::addUnsafeEnchantment);

            for (Player target : targets) {
                target.getInventory().addItem(stack);
            }

            sender.sendMessage(F.fMain(this) + "Gave " + F.fItem(stack) + " to " + F.fList((String[]) Arrays.stream(targets).map(Player::getName).toArray()) + ".");
        }
    }

    @SuppressWarnings("ReassignedVariable")
    private Material[] materialSearch(String targetName) {
        targetName = targetName.toLowerCase();

        List<Material> materialMatches = new ArrayList<>();
        for (Material material : Material.values()) {
            String materialName = material.name();
            if (materialName.equalsIgnoreCase(targetName)) {
                materialMatches.clear();
                materialMatches.add(material);
                break;
            }
            if (!materialName.toLowerCase().contains(targetName)) {
                continue;
            }
            materialMatches.add(material);
        }
        return materialMatches.toArray(new Material[0]);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                names.add(".");
                names.add("*");
                names.add("**");
                names.addAll(_miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().filter(target -> {
                    if (sender instanceof Player player) {
                        return player.canSee(target);
                    }
                    return true;
                }).map(Player::getName).toList());
            }
            case 2 -> {
                for (Material material : Material.values()) {
                    names.add(material.name());
                }
            }
            case 3 -> {
                for (int i = 1; i <= 64; i++) {
                    names.add(Integer.toString(i));
                }
            }
            default -> {
                for (Enchantment enchantment : Enchantment.values()) {
                    names.add(enchantment.getName());
                }
            }
        }
        return names;
    }

    @Override
    public String help(String alias) {
        return super.help(alias) + "\n" +
                F.fMain() + "Player selectors:\n" +
                F.fList(1, F.fElem(".") + " - Yourself\n") +
                F.fList(1, F.fElem("*") + " - Everyone\n") +
                F.fList(1, F.fElem("**") + " - Others");
    }
}
