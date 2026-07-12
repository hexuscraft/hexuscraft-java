package net.hexuscraft.core.actionbar;

import org.bukkit.entity.Player;

public final class ActionBar {
	public final Player _player;
	private final CoreActionBar _coreActionBar;
	private int _weight;
	private String _message;

	public ActionBar(CoreActionBar coreActionBar, Player player, int weight, String message) {
		_coreActionBar = coreActionBar;
		_player = player;
		_weight = weight;
		_message = message;
	}

	public int getWeight() {
		return _weight;
	}

	public void setWeight(int weight) {
		_weight = weight;
		_coreActionBar.updateActionBars();
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		_message = message;
		_coreActionBar.updateActionBars();
	}

}
