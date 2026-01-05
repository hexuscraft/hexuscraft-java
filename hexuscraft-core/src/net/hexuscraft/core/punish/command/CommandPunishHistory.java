package net.hexuscraft.core.punish.command;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.MiniPluginPunish;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CommandPunishHistory extends BaseCommand<MiniPluginPunish> {

    public CommandPunishHistory(MiniPluginPunish miniPluginPunish) {
        super(miniPluginPunish, "punishmenthistory", "[Player]", "View the history of punishments.",
                Set.of("punishhistory", "xh"), MiniPluginPunish.PERM.COMMAND_PUNISH_HISTORY);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof final Player senderPlayer)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can view punishment history.")));
            return;
        }

        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            final OfflinePlayer targetOfflinePlayer;

            if (args.length == 1) {
                targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(args[0], sender);
                if (targetOfflinePlayer == null) return;
            } else {
                targetOfflinePlayer = senderPlayer;
            }

            openGui(senderPlayer, targetOfflinePlayer);
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers =
                    _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof final Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }

            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }


    public void openGui(final Player sender, final OfflinePlayer targetOfflinePlayer) {
        Inventory gui = _miniPlugin._hexusPlugin.getServer()
                .createInventory(sender, 6 * 9, "Punish History - " + targetOfflinePlayer.getName());

        ItemStack targetSkull = UtilItem.createItemSkull(targetOfflinePlayer.getName(),
                C.cGreen + C.fBold + targetOfflinePlayer.getName(), targetOfflinePlayer.getUniqueId().toString(), "",
                C.cWhite + "Viewing punishment history");

        ItemStack openPunishGui = UtilItem.createItem(Material.NAME_TAG, C.cBlue + C.fBold + "Apply Punishment",
                "Open the punishment menu for " + F.fItem(targetOfflinePlayer.getName()));

        if (sender.hasPermission(PermissionGroup.TRAINEE.name())) gui.setItem(53, openPunishGui);

        gui.setItem(4, targetSkull);

        sender.openInventory(gui);
    }
}
