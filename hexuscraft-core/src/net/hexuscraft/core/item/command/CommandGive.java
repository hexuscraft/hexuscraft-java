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

        final Player[] targets;

        switch (args[0]) {
            case "." -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(F.fMain(this) + "Only players can use this selector.");
                    return;
                }
                targets = new Player[]{(Player) sender};
            }
            case "*" -> targets = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().toArray(new Player[0]);

            case "**" ->
                    targets = (Player[]) _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().filter(player -> !player.getName().equals(sender.getName())).toArray();

            default ->
                    targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0]);
        }

        if (targets.length == 0) {
            sender.sendMessage(F.fMain(this) + "Found no players with name " + F.fItem(args[0]) + ".");
            return;
        }

        final int amount = args.length > 2 ? Integer.parseInt(args[2]) % 64 : 1;

        final HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (args.length > 3) {
            Arrays.stream(args[3].split(",")).map(s -> s.split(":")).forEach(strings -> {
                Enchantment enchantment = Enchantment.getByName(strings[0]);
                if (enchantment == null) {
                    sender.sendMessage(F.fMain(this) + "Unknown enchantment named " + F.fItem(strings[0]) + ". Listing Enchantments:\n"
                            + F.fMain() + F.fList(Arrays.stream(Enchantment.values()).map(Enchantment::getName).toArray(String[]::new)));
                    return;
                }

                //noinspection ReassignedVariable
                int enchantmentLevel = 1;
                if (strings.length > 1) {
                    try {
                        enchantmentLevel = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(F.fMain(this) + "Unknown enchantment level " + F.fItem(strings[1]));
                        return;
                    }
                }

                enchantmentMap.put(enchantment, enchantmentLevel);
            });
        }

        for (String materialSearchName : args[1].split(",")) {
            Material[] targetMaterials = materialSearch(materialSearchName);
            if (targetMaterials.length != 1) {
                StringBuilder builder = new StringBuilder();
                builder.append(F.fMain("Material Search")).append(F.fItem(targetMaterials.length + " Matches")).append(" for ").append(F.fItem(materialSearchName)).append(".");
                if (targetMaterials.length > 1) {
                    builder.append(F.fMain()).append(F.fList(Arrays.stream(targetMaterials).map(Material::name).toArray(String[]::new)));
                }
                sender.sendMessage(builder.toString());
                continue;
            }

            ItemStack stack = new ItemStack(targetMaterials[0]);
            stack.setAmount(amount);

            stack.addUnsafeEnchantments(enchantmentMap);

            for (Player target : targets) {
                target.getInventory().addItem(stack);
            }

            sender.sendMessage(F.fMain(this) + "Gave " + F.fItem(stack) + " to " + F.fList(Arrays.stream(targets).map(Player::getName).toArray(String[]::new)));
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
            case 2 -> names.addAll(Arrays.stream(Material.values()).map(Material::name).toList());
            case 3 -> {
                for (int i = 1; i <= 64; i++) {
                    names.add(Integer.toString(i));
                }
            }
            default -> names.addAll(Arrays.stream(Enchantment.values()).map(Enchantment::getName).toList());
        }
        return names;
    }

    @Override
    public String help(String alias) {
        return super.help(alias) + "\n"
                + F.fMain() + "Player selectors:\n"
                + F.fList(1) + "Yourself - " + F.fItem(".") + "\n"
                + F.fList(2) + "Everyone - " + F.fItem("*") + "\n"
                + F.fList(3) + "Others - " + F.fItem("**");
    }

}
