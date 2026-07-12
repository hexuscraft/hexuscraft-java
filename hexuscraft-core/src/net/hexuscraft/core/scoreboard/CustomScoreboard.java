package net.hexuscraft.core.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class CustomScoreboard {

	public CustomSidebar _sidebar;
	Player _player;
	Scoreboard _bukitScoreboard;

	CustomScoreboard(Player player, Scoreboard bukkitScoreboard) {
		_player = player;
		_bukitScoreboard = bukkitScoreboard;
		_sidebar = new CustomSidebar(this);
	}

}
