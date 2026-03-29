package net.hexuscraft.hub.hubscoreboard;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
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

import java.util.*;

public class HubScoreboard extends MiniPlugin<Hub>
{

    private final Map<Player, BukkitTask> _sidebarUpdateTasks;
    private CorePortal _corePortal;
    private CorePermission _corePermission;

    public HubScoreboard(Hub hub)
    {
        super(hub, "Scoreboard");

        _sidebarUpdateTasks = new HashMap<>();
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
        _corePermission = (CorePermission) dependencies.get(CorePermission.class);
    }

    @Override
    public void onEnable()
    {
        _hexusPlugin.getServer()
                .getOnlinePlayers()
                .stream()
                .map(player -> new PlayerJoinEvent(player, null))
                .forEach(this::onPlayerJoin);
    }

    @Override
    public void onDisable()
    {
        _sidebarUpdateTasks.values().forEach(BukkitTask::cancel);
        _sidebarUpdateTasks.clear();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Scoreboard scoreboard = player.getScoreboard(); // Player's scoreboard is set by core MiniPluginScoreboard

        Objective sidebarObjective = scoreboard.registerNewObjective("§6§lHEXUSCRAFT", "dummy");
        sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Score> sidebarScores = new ArrayList<>();
        _sidebarUpdateTasks.put(player, _hexusPlugin.runSyncTimer(() ->
        {
            sidebarScores.stream().map(Score::getEntry).forEach(scoreboard::resetScores);
            sidebarScores.clear();

            String[] lines = generateSidebarLines(player);
            for (int i = 0; i < lines.length; i++)
            {
                String line = lines[lines.length - i - 1];
                Score score = sidebarObjective.getScore(C.hexMap.get(i) + C.fReset + line);
                score.setScore(i);
                sidebarScores.add(score);
            }
        }, 0, 20));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (!_sidebarUpdateTasks.containsKey(player))
        {
            return;
        }

        _sidebarUpdateTasks.get(player).cancel();
        _sidebarUpdateTasks.remove(player);
    }

    private String[] generateSidebarLines(Player player)
    {
        return new String[]{C.cAqua + C.fBold + "Server",
                _corePortal._serverName,
                "",
                C.cGreen + C.fBold + "Players",
                "" + Arrays.stream(_corePortal.getServers()).mapToInt(s -> s._players).sum(),
                "",
                C.cYellow + C.fBold + "Coins",
                "0",
                "",
                C.cGold + C.fBold + "Rank",
                PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(player)
                        ._groups())._prefix,
                "",
                C.cRed + C.fBold + "Website",
                "www.hexuscraft.net"};
    }

}
