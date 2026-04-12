package net.hexuscraft.core.punish;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record PunishGui(Inventory _inventory,
                        OfflinePlayer _target,
                        String _reason,
                        ItemStack _skull,
                        ItemStack _warning,
                        ItemStack _mute1d,
                        ItemStack _mute3d,
                        ItemStack _mute5d,
                        ItemStack _mute7d,
                        ItemStack _mute14d,
                        ItemStack _mute28d,
                        ItemStack _mutePerm,
                        ItemStack _ban1d,
                        ItemStack _ban3d,
                        ItemStack _ban5d,
                        ItemStack _ban7d,
                        ItemStack _ban14d,
                        ItemStack _ban28d,
                        ItemStack _banPerm)
{
}
