package net.hexuscraft.core.punish;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record PunishGui(Inventory _inventory,
                        OfflinePlayer _target,
                        String _reason,
                        ItemStack _chatSev1,
                        ItemStack _chatSev2,
                        ItemStack _chatSev3,
                        ItemStack _gameplaySev1,
                        ItemStack _gameplaySev2,
                        ItemStack _gameplaySev3,
                        ItemStack _clientSev1,
                        ItemStack _clientSev2,
                        ItemStack _clientSev3,
                        ItemStack _warning,
                        ItemStack _permanentMute,
                        ItemStack _permanentBan,
                        ItemStack _viewHistory)
{
}
