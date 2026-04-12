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

public class CoreScoreboard extends MiniPlugin<HexusPlugin>
{

    final Map<Player, Scoreboard> _scoreboardMap;

    public CoreScoreboard(HexusPlugin plugin)
    {
        super(plugin, "Scoreboard");

        _scoreboardMap = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        for (Player player : _hexusPlugin.getServer().getOnlinePlayers())
        {
            onPlayerJoin(new PlayerJoinEvent(player, null));
        }
    }

    @Override
    public void onDisable()
    {
        for (Player player : _hexusPlugin.getServer().getOnlinePlayers())
        {
            onPlayerQuit(new PlayerQuitEvent(player, null));
        }
        _scoreboardMap.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Scoreboard scoreboard = _hexusPlugin.getServer().getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        _scoreboardMap.put(player, scoreboard);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        player.setScoreboard(_hexusPlugin.getServer().getScoreboardManager().getMainScoreboard());

        if (!_scoreboardMap.containsKey(player))
        {
            return;
        }
        Scoreboard scoreboard = _scoreboardMap.get(player);
        scoreboard.getTeams().forEach(Team::unregister);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        scoreboard.getObjectives().forEach(Objective::unregister);
        _scoreboardMap.remove(player);
    }

}
