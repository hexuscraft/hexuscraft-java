package net.hexuscraft.hub.team;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionProfile;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MiniPluginTeam extends MiniPlugin<Hub> {

    private MiniPluginPermission _miniPluginPermission;

    public MiniPluginTeam(Hub hub) {
        super(hub, "Teams");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard scoreboard = player.getScoreboard();

        final Map<PermissionGroup, Team> teams = new HashMap<>();

        Arrays.stream(PermissionGroup.values())
                .forEach((final PermissionGroup group) -> {
                    final Team team = scoreboard.registerNewTeam(group._prefix);
                    teams.put(group, team);
                    team.setPrefix(F.fPermissionGroup(group, true, true) + C.fReset + " ");
                });

        _miniPluginPermission._permissionProfiles.forEach(
                (final Player player1, final PermissionProfile permissionProfile) -> {
                    final PermissionGroup highestGroup = PermissionGroup.getGroupWithHighestWeight(
                            permissionProfile._groups());
                    teams.computeIfPresent(highestGroup, (group, team) -> {
                        team.addEntry(player1.getName());
                        return team;
                    });

                });
    }

}
