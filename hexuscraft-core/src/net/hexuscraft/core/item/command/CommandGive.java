package net.hexuscraft.core.item.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.PluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandGive extends BaseCommand {

    public CommandGive(PluginItem itemCenter) {
        super(itemCenter, "give", "<Player> <Item> [Amount] [Enchantments]", "Give items to players.", Set.of("g", "item", "i"), PluginItem.PERM.COMMAND_GIVE);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length != 1) {
                return;
            }
            Player target = targets[0];
        }

        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        switch (args.length) {
            case 1 ->
                    _miniPlugin._javaPlugin.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
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

}
