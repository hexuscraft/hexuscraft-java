package net.hexuscraft.core.item.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.PluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandClear extends BaseCommand {

    public CommandClear(PluginItem pluginItem) {
        super(pluginItem, "clear", "[Players]", "Clear the inventory of targets.", Set.of("clearinventory"), PluginItem.PERM.COMMAND_CLEAR);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0], sender);
        if (targets.length == 0) {
            return;
        }

        for (Player target : targets) {
            target.getInventory().clear();
        }

        sender.sendMessage(F.fMain(this) + "Cleared the inventories of " + F.fList(Arrays.stream(targets).map(Player::getName).toArray(String[]::new)) + ".");
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> completions = new ArrayList<>(List.of("*", "**", "."));
        completions.addAll(_miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        return completions;
    }
}
