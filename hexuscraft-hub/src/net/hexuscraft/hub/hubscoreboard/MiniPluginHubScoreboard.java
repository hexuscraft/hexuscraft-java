package net.hexuscraft.hub.hubscoreboard;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.core.scoreboard.MiniPluginScoreboard;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;

public final class MiniPluginHubScoreboard extends MiniPlugin<Hub> {

    private MiniPluginScoreboard _miniPluginScoreboard;
    private MiniPluginPortal _miniPluginPortal;
    private MiniPluginPermission _miniPluginPermission;

    public MiniPluginHubScoreboard(final Hub hub) {
        super(hub, "Hub Scoreboard");
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginScoreboard = (MiniPluginScoreboard) dependencies.get(MiniPluginScoreboard.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = player.getScoreboard(); // Player's scoreboard is set by core MiniPluginScoreboard

        final Objective objective = scoreboard.registerNewObjective("§6§lHEXUSCRAFT", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        final String[] lines = new String[]{
                C.cAqua + C.fBold + "Server", _miniPluginPortal._serverName, "",
                C.cGreen + C.fBold + "Experience", "WIP", "",
                C.cYellow + C.fBold + "Coins", "WIP", "",
                C.cGold + C.fBold + "Rank", PermissionGroup.getGroupWithHighestWeight(
                _miniPluginPermission._permissionProfiles.get(player)._groups())._prefix, "",
                C.cRed + C.fBold + "Website", "www.hexuscraft.net",};

        for (int i = 0; i < lines.length; i++) {
            objective.getScore(C.hexMap.get(i) + C.fReset + lines[lines.length - i - 1]).setScore(i);
        }
    }
}
