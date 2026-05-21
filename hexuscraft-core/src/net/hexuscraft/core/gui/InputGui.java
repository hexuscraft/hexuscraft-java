package net.hexuscraft.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class InputGui extends Gui {
    public InputGui(Player player) {
        super(GuiType.INPUT, player);
        _inventory = player.getServer().createInventory(player, InventoryType.ANVIL);
    }
}
