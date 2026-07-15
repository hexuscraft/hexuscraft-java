package net.hexuscraft.core.scoreboard;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class CoreScoreboard extends MiniPlugin<HexusPlugin> {

	public final Map<Player, CustomScoreboard> _customScoreboards;

	public CoreScoreboard(HexusPlugin plugin) {
		super(plugin, "Scoreboard");
		_customScoreboards = new HashMap<>();
	}

	@Override
	public void onEnable() {
		_hexusPlugin.getServer()
			.getOnlinePlayers()
			.stream()
			.map(player -> new PlayerJoinEvent(player, null))
			.forEach(this::onPlayerJoin);
	}

	@Override
	public void onDisable() {
		_hexusPlugin.getServer()
			.getOnlinePlayers()
			.stream()
			.map(player -> new PlayerQuitEvent(player, null))
			.forEach(this::onPlayerQuit);
		_customScoreboards.clear();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CustomScoreboard customScoreboard = new CustomScoreboard(this,
			player,
			_hexusPlugin.getServer().getScoreboardManager().getNewScoreboard());
		_customScoreboards.put(player, customScoreboard);
		player.setScoreboard(customScoreboard._bukitScoreboard);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		player.setScoreboard(_hexusPlugin.getServer().getScoreboardManager().getMainScoreboard());
		_customScoreboards.remove(player);
	}

}
