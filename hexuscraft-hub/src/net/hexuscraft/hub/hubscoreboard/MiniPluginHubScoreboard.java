package net.hexuscraft.hub.hubscoreboard;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MiniPluginHubScoreboard extends MiniPlugin<Hub> {

    private final Map<Player, BukkitTask> _sidebarUpdateTasks;
    private MiniPluginPortal _miniPluginPortal;
    private MiniPluginPermission _miniPluginPermission;

    public MiniPluginHubScoreboard(final Hub hub) {
        super(hub, "Hub Scoreboard");

        _sidebarUpdateTasks = new HashMap<>();
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
    }

    @Override
    public void onEnable() {
        _hexusPlugin.getServer().getOnlinePlayers().stream().map(player -> new PlayerJoinEvent(player, null))
                .forEach(this::onPlayerJoin);
    }

    @Override
    public void onDisable() {
        _sidebarUpdateTasks.values().forEach(BukkitTask::cancel);
        _sidebarUpdateTasks.clear();
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = player.getScoreboard(); // Player's scoreboard is set by core MiniPluginScoreboard

        final Objective sidebarObjective = scoreboard.registerNewObjective("§6§lHEXUSCRAFT", "dummy");
        sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        final List<Score> sidebarScores = new ArrayList<>();
        _sidebarUpdateTasks.put(player, _hexusPlugin.runSyncTimer(() -> {
            sidebarScores.stream().map(Score::getEntry).forEach(scoreboard::resetScores);
            sidebarScores.clear();

            final String[] lines = generateSidebarLines(player);
            for (int i = 0; i < lines.length; i++) {
                final String line = lines[lines.length - i - 1];
                final Score score = sidebarObjective.getScore(C.hexMap.get(i) + C.fReset + line);
                score.setScore(i);
                sidebarScores.add(score);
            }
        }, 0, 10));
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (_sidebarUpdateTasks.containsKey(player)) {
            _sidebarUpdateTasks.get(player).cancel();
            _sidebarUpdateTasks.remove(player);
        }
    }

    private String[] generateSidebarLines(final Player player) {
        return new String[]{
                C.cAqua + C.fBold + "Server", _miniPluginPortal._serverName, "",
                C.cGreen + C.fBold + "Players",
                "" + _miniPluginPortal._serverCache.stream().mapToInt(s -> s._players).sum(), "",
                C.cYellow + C.fBold + "Coins", "0", "",
                C.cGold + C.fBold + "Rank", PermissionGroup.getGroupWithHighestWeight(
                _miniPluginPermission._permissionProfiles.get(player)._groups())._prefix, "",
                C.cRed + C.fBold + "Website", "www.hexuscraft.net"};
    }

}
