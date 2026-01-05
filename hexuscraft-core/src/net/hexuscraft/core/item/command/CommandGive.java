package net.hexuscraft.core.item.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.ItemSearch;
import net.hexuscraft.core.item.MiniPluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public final class CommandGive extends BaseCommand<MiniPluginItem> {

    public CommandGive(final MiniPluginItem itemCenter) {
        super(itemCenter, "give", "<Players> <Item> [Amount] [Enchantment:Level Enchantment:Level ...]",
                "Give yourself an item or give items to players with optional enchantments.", Set.of("g", "item", "i"),
                MiniPluginItem.PERM.COMMAND_GIVE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player[] matches =
                PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0],
                        sender, predicateMatches -> predicateMatches.length == 0);
        if (matches.length == 0) return;

        final String[] materialNames = args[1].split(",");
        if (materialNames.length > 36) {
            // There are only 36 slots in a player inventory! (9 * 4)
            sender.sendMessage(F.fMain(this, F.fError("Cannot give more than 36 materials.")));
            return;
        }

        final AtomicInteger amount = new AtomicInteger(1);
        if (args.length > 2) try {
            amount.set(Integer.parseInt(args[2]));
        } catch (final NumberFormatException ex) {
            sender.sendMessage(
                    F.fMain(this, F.fError("Invalid amount, expected a positive integer. "), "Defaulting to ",
                            F.fItem(amount.get()), "."));
        }

        if (amount.get() < 1) {
            sender.sendMessage(F.fMain(this, F.fError("Cannot give less than 1 item.")));
            return;
        }

        if (materialNames.length * amount.get() > 2304) {
            // A player can only carry up to 2304 items in their inventory. (64 * 9 * 4)
            sender.sendMessage(F.fMain(this, F.fError("Cannot give more than 2304 items.")));
            return;
        }

        final HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
        if (args.length > 3) {
            Arrays.stream(Arrays.copyOfRange(args, 3, args.length)).map(s -> s.split(":")).forEach(strings -> {
                Enchantment enchantment = Enchantment.getByName(strings[0]);
                if (enchantment == null) {
                    sender.sendMessage(F.fMain(this) + "Unknown enchantment named " + F.fItem(strings[0]) +
                            ". Listing Enchantments:\n" + F.fMain("") +
                            F.fItem(Arrays.stream(Enchantment.values()).map(Enchantment::getName)
                                    .toArray(String[]::new)));
                    return;
                }

                final AtomicInteger enchantmentLevel = new AtomicInteger(1);
                if (strings.length > 1) {
                    try {
                        enchantmentLevel.set(Integer.parseInt(strings[1]));
                    } catch (final NumberFormatException ex) {
                        sender.sendMessage(
                                F.fMain(this, F.fError("Unknown enchantment level ", F.fItem(strings[1]), ". "),
                                        "Defaulting to ", F.fItem(enchantmentLevel.get()), "."));
                    }
                }

                enchantmentMap.put(enchantment, enchantmentLevel.get());
            });
        }

        for (final String arg : args[1].split(",")) {
            final String[] argSplitted = arg.split(":", 2);
            final String materialName = argSplitted.length > 0 ? argSplitted[0] : null;
            final AtomicReference<Byte> data = new AtomicReference<>((byte) 0);

            final Material[] targetMaterials =
                    ItemSearch.itemSearch(materialName, sender, materials -> materials.length != 1);
            if (targetMaterials.length != 1) continue;

            if (argSplitted.length > 1) try {
                data.set(Byte.parseByte(argSplitted[1]));
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("Invalid material punish for ", F.fItem(targetMaterials[0].name()),
                                ", expected an integer. "), "Defaulting to ", F.fItem(data.get()), "."));
            }

            final AtomicInteger remainingAmount = new AtomicInteger(amount.get());
            while (remainingAmount.get() > 0) {
                final ItemStack stack =
                        new ItemStack(targetMaterials[0], Math.min(remainingAmount.get(), 64), data.get());
                stack.addUnsafeEnchantments(enchantmentMap);

                for (final Player target : matches) {
                    target.getInventory().addItem(stack);
                }

                remainingAmount.set(remainingAmount.get() - 64);
            }

            sender.sendMessage(F.fMain(this, "Gave ", F.fItem(amount + " " + targetMaterials[0].name()), " to ",
                    F.fItem(Arrays.stream(matches).map(Player::getName).toArray(String[]::new)), "."));
        }
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                //noinspection ReassignedVariable
                Stream<? extends Player> streamedOnlinePlayers =
                        _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
                if (sender instanceof final Player player) {
                    streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
                }

                names.addAll(List.of("*", "**"));
                names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
            }
            case 2 -> names.addAll(
                    Arrays.stream(Material.values()).filter(ItemSearch::isMaterialAnItem).map(Material::name).toList());
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
