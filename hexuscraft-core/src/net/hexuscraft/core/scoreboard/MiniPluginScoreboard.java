package net.hexuscraft.core.scoreboard;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public final class MiniPluginScoreboard extends MiniPlugin<HexusPlugin> {

    private final Map<Player, Scoreboard> _scoreboardMap;

    public MiniPluginScoreboard(final HexusPlugin plugin) {
        super(plugin, "Scoreboard");

        _scoreboardMap = new HashMap<>();
    }

    @Override
    public void onEnable() {
        for (Player player : _hexusPlugin.getServer().getOnlinePlayers()) {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Override
    public void onDisable() {
        for (Player player : _hexusPlugin.getServer().getOnlinePlayers()) {
            onPlayerQuit(new PlayerQuitEvent(player, null));
        }
        _scoreboardMap.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = _hexusPlugin.getServer().getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        _scoreboardMap.put(player, scoreboard);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        player.setScoreboard(_hexusPlugin.getServer().getScoreboardManager().getMainScoreboard());

        if (!_scoreboardMap.containsKey(player)) return;
        final Scoreboard scoreboard = _scoreboardMap.get(player);
        scoreboard.getTeams().forEach(Team::unregister);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        scoreboard.getObjectives().forEach(Objective::unregister);
        _scoreboardMap.remove(player);
    }

}
