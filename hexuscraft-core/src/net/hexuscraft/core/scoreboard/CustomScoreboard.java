package net.hexuscraft.core.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class CustomScoreboard {

	public CustomSidebar _sidebar;
	CoreScoreboard _coreScoreboard;
	Player _player;
	Scoreboard _bukitScoreboard;

	CustomScoreboard(CoreScoreboard coreScoreboard, Player player, Scoreboard bukkitScoreboard) {
		_coreScoreboard = coreScoreboard;
		_player = player;
		_bukitScoreboard = bukkitScoreboard;
		_sidebar = new CustomSidebar(this);
	}

}
