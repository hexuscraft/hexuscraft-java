package net.hexuscraft.core.scoreboard;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

// TODO: Race condition with mini plugins which modify the scoreboard. Set low event priority.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Scoreboard scoreboard = _hexusPlugin.getServer().getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        _scoreboardMap.put(player, scoreboard);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.setScoreboard(_hexusPlugin.getServer().getScoreboardManager().getMainScoreboard());

        Scoreboard scoreboard = _scoreboardMap.get(player);
        scoreboard.getTeams().forEach(Team::unregister);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        scoreboard.getObjectives().forEach(Objective::unregister);
        _scoreboardMap.remove(player);
    }

}
