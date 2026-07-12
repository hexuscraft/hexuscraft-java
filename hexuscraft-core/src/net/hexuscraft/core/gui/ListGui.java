package net.hexuscraft.core.gui;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.core.item.UtilItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ListGui extends Gui {
	GuiItem[] _items;

	public ListGui(Player player, String title, GuiItem... items) {
		super(GuiType.LIST, player);
		_items = items;
		_inventory = player.getServer().createInventory(player, ((items.length + 18 + 8) / 9) * 9, title);
		populateInventory();
	}

	void populateInventory() {
		int itemIndex = 0;
		for (int i = 0; i < _inventory.getSize(); i++) {
			if (i < 9 || i >= _inventory.getSize() - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
				//noinspection deprecation
				_inventory.setItem(i,
					UtilItem.createWithData(Material.STAINED_GLASS_PANE, DyeColor.GRAY.getData(), C.fReset));
				continue;
			}
			if (_items.length == itemIndex) {
				continue;
			}
			_inventory.setItem(i, _items[itemIndex]._itemStack);
			itemIndex += 1;
		}
	}
}
