package net.hexuscraft.hub.hubscoreboard;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.scoreboard.MiniPluginScoreboard;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;

public final class MiniPluginHubScoreboard extends MiniPlugin<Hub> {

    private MiniPluginScoreboard _miniPluginScoreboard;

    public MiniPluginHubScoreboard(final Hub hub) {
        super(hub, "Hub Scoreboard");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginScoreboard = (MiniPluginScoreboard) dependencies.get(MiniPluginScoreboard.class);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = player.getScoreboard(); // Player's scoreboard is set by core MiniPluginScoreboard

        final Objective objective = scoreboard.registerNewObjective("§6§lHEXUSCRAFT", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        final Score line1 = objective.getScore("§1§r" + C.cGold + C.fBold + "Testing !");
        line1.setScore(1);
    }

}
