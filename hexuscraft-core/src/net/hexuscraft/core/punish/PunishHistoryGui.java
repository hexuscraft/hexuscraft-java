package net.hexuscraft.core.punish;

import net.hexuscraft.common.database.data.PunishData;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public record PunishHistoryGui(Inventory _inventory,
                               OfflinePlayer _target,
                               BukkitTask _loadingTask,
                               BukkitTask _fetchTask,
                               Map<ItemStack, PunishData> _punishments) {
}
