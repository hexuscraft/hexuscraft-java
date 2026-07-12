package net.hexuscraft.hub.player;

import net.hexuscraft.common.database.data.ServerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record LobbyMenu(Player _player, Inventory _inventory, Map<ItemStack, ServerData> _lobbies) {
}
