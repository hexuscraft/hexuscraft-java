package net.hexuscraft.hub.hubscoreboard;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public final class MiniPluginHubScoreboard extends MiniPlugin<Hub> {

    public MiniPluginHubScoreboard(final Hub hub) {
        super(hub, "Hub Scoreboard");
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = _hexusPlugin.getServer().getScoreboardManager().getNewScoreboard();

        final Objective objective = scoreboard.registerNewObjective("§6§lHEXUSCRAFT", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        final Score line1 = objective.getScore(C.cGold + C.fBold + "Testing !");
        line1.setScore(1);

        player.setScoreboard(scoreboard);
    }

}
