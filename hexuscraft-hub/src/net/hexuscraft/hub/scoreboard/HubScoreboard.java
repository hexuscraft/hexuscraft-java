package net.hexuscraft.hub.scoreboard;

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
import java.util.concurrent.atomic.AtomicInteger;

public class HubScoreboard extends MiniPlugin<Hub>
{

    final Map<Player, List<BukkitTask>> _sidebarUpdateTasks;
    private final String SIDEBAR_TITLE = "          Welcome %s, to the Hexuscraft Network!";
    CorePortal _corePortal;
    CorePermission _corePermission;

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
        _sidebarUpdateTasks.values().stream().flatMap(Collection::stream).forEach(BukkitTask::cancel);
        _sidebarUpdateTasks.clear();
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Scoreboard scoreboard = player.getScoreboard(); // Player's scoreboard is set by CoreScoreboard

        String sidebarTitle = SIDEBAR_TITLE.formatted(player.getName());
        AtomicInteger sidebarTitleIndex = new AtomicInteger();

        Objective sidebarObjective =
                scoreboard.registerNewObjective(sidebarTitle.substring(0, Math.min(16, sidebarTitle.length())),
                        "dummy");
        sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Score> sidebarScores = new ArrayList<>();

        List<BukkitTask> sidebarTasks = new ArrayList<>();
        _sidebarUpdateTasks.put(player, sidebarTasks);

        sidebarTasks.add(_hexusPlugin.runSyncTimer(() ->
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

        if (sidebarTitle.length() > 16)
        {
            sidebarTasks.add(_hexusPlugin.runSyncTimer(() ->
            {
                int index = sidebarTitleIndex.getAndUpdate(operand -> (operand + 1) % sidebarTitle.length());

                sidebarObjective.setDisplayName(C.cWhite +
                        C.fBold +
                        (index + 16 > sidebarTitle.length() ?
                                sidebarTitle.substring(index) +
                                        sidebarTitle.substring(0, 16 - (sidebarTitle.length() - index)) :
                                sidebarTitle.substring(index, index + 16)));
            }, 0, 4));
        }
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        if (!_sidebarUpdateTasks.containsKey(player))
        {
            return;
        }

        _sidebarUpdateTasks.get(player).forEach(BukkitTask::cancel);
        _sidebarUpdateTasks.remove(player);
    }

    String[] generateSidebarLines(Player player)
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
