package net.hexuscraft.core.report;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record ReportGui(Inventory inventory,
                        OfflinePlayer _target,
                        ItemStack chat,
                        ItemStack gameplay,
                        ItemStack client,
                        ItemStack misc,
                        ItemStack history)
{
}
