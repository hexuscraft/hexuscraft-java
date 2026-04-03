package net.hexuscraft.core.report;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record ReportGui(Inventory _inventory,
                        OfflinePlayer _target,
                        String _message,
                        ItemStack _chat,
                        ItemStack _gameplay,
                        ItemStack _client,
                        ItemStack _misc,
                        ItemStack _history)
{
}
