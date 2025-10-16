package net.hexuscraft.core.item.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.MaterialSearch;
import net.hexuscraft.core.item.MiniPluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public final class CommandGive extends BaseCommand<MiniPluginItem> {

    public CommandGive(final MiniPluginItem itemCenter) {
        super(itemCenter, "give", "<Players> <Item> [Amount] [Enchantment:Level ...]", "Give items to players.", Set.of("g", "item", "i"), MiniPluginItem.PERM.COMMAND_GIVE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
        if (targets.length == 0) return;

        final String[] materialNames = args[1].split(",");
        if (materialNames.length > 36) {
            // There are only 36 slots in a player inventory! (9 * 4)
            sender.sendMessage(F.fMain(this, F.fError("Cannot give more than 36 materials.")));
            return;
        }

        final int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        if (materialNames.length * amount > 2304) {
            // A player can only carry up to 2304 items in their inventory. (64 * 9 * 4)
            sender.sendMessage(F.fMain(this, F.fError("Cannot give more than 2304 items.")));
            return;
        }

        final HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (args.length > 3) {
            Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).map(s -> s.split(":")).forEach(strings -> {
                Enchantment enchantment = Enchantment.getByName(strings[0]);
                if (enchantment == null) {
                    sender.sendMessage(F.fMain(this) + "Unknown enchantment named " + F.fItem(strings[0]) + ". Listing Enchantments:\n"
                            + F.fMain("") + F.fList(Arrays.stream(Enchantment.values()).map(Enchantment::getName).toArray(String[]::new)));
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
            Material[] targetMaterials = MaterialSearch.materialSearch(materialSearchName, sender);
            if (targetMaterials.length != 1) {
                continue;
            }

            int remainingAmount = amount;
            while (remainingAmount > 0) {
                ItemStack stack = new ItemStack(targetMaterials[0]);
                stack.setAmount(Math.min(remainingAmount, 64));
                stack.addUnsafeEnchantments(enchantmentMap);

                for (Player target : targets) {
                    target.getInventory().addItem(stack);
                }

                remainingAmount -= 64;
            }

            sender.sendMessage(F.fMain(this) + "Gave " + F.fItem(targetMaterials[0], amount) + " to " + F.fList(Arrays.stream(targets).map(Player::getName).toArray(String[]::new)));
        }
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
                if (sender instanceof final Player player) {
                    streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
                }

                names.addAll(List.of("*", "**"));
                names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
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

}
