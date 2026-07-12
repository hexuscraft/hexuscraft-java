package net.hexuscraft.core.portal.gui;

import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.core.gui.GuiItem;
import net.hexuscraft.core.gui.ListGui;
import net.hexuscraft.core.item.UtilItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GuiCreateServerGroup extends ListGui {
	public GuiCreateServerGroup(Player player, ServerGroupData.Builder builder) {
		GuiItem name = new GuiItem(UtilItem.create(Material.PAPER, C.cGreen + C.fBold + "Name", builder._name));
		GuiItem capacity = new GuiItem(UtilItem.create(Material.WATCH,
			C.cGreen + C.fBold + "Capacity",
			Integer.toString(builder._capacity)));
		GuiItem games = new GuiItem(UtilItem.create(Material.CARROT_STICK,
			C.cGreen + C.fBold + "Games",
			Arrays.stream(builder._games).map(gameType -> gameType._name).toArray(String[]::new)));
		GuiItem hostUUID = new GuiItem(UtilItem.createPlayerSkull(player.getName(),
			C.cGreen + C.fBold + "Server Host",
			builder._hostUUID.toString()));

		super(player, "Create Server Group", name, capacity, games, hostUUID);
	}
}
