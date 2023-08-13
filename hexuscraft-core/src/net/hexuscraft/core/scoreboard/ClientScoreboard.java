package net.hexuscraft.core.scoreboard;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.MiniPluginClient;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

public class ClientScoreboard extends MiniPluginClient {

    Scoreboard scoreboard;

    ClientScoreboard(PluginScoreboard pluginScoreboard, Player player) {
        super(pluginScoreboard, player);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> miniPluginClassMap) {
        scoreboard = _javaPlugin.getServer().getScoreboardManager().getNewScoreboard();
    }

    @Override
    public void onEnable() {
        _player.setScoreboard(scoreboard);
    }

    @Override
    public void onDisable() {
        scoreboard.getTeams().forEach(Team::unregister);
        scoreboard.getEntries().forEach(s -> scoreboard.resetScores(s));
        scoreboard.getObjectives().forEach(Objective::unregister);

        _player.setScoreboard(_javaPlugin.getServer().getScoreboardManager().getMainScoreboard());

        scoreboard = null;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}