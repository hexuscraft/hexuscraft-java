package net.hexuscraft.core.item.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.MiniPluginItem;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandClear extends BaseCommand<MiniPluginItem> {

    public CommandClear(final MiniPluginItem miniPluginItem) {
        super(miniPluginItem, "clear", "[Players]", "Clear the inventory of targets.", Set.of("clearinventory"), MiniPluginItem.PERM.COMMAND_CLEAR);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender, matches -> matches.length == 0);
        if (targets.length == 0) return;

        Arrays.stream(targets).forEach(target -> target.getInventory().clear());
        sender.sendMessage(F.fMain(this) + "Cleared the inventories of " + F.fList(Arrays.stream(targets).map(Player::getName).toArray(String[]::new)) + ".");
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> completions = new ArrayList<>(List.of("*", "**", "."));
        completions.addAll(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        return completions;
    }
}
