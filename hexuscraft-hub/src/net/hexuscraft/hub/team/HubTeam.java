package net.hexuscraft.hub.team;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.permission.PermissionProfile;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.Map;

public class HubTeam extends MiniPlugin<Hub>
{

    private CorePermission _corePermission;

    public HubTeam(Hub hub)
    {
        super(hub, "Team");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _corePermission = (CorePermission) dependencies.get(CorePermission.class);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        Player eventPlayer = event.getPlayer();
        Scoreboard eventPlayerScoreboard = eventPlayer.getScoreboard();

        Arrays.stream(PermissionGroup.values())
                .filter(permissionGroup -> eventPlayerScoreboard.getTeam(permissionGroup._prefix) == null)
                .forEach((PermissionGroup group) ->
                {
                    Team team = eventPlayerScoreboard.registerNewTeam(group._prefix);
                    team.setPrefix(F.fPermissionGroup(group, true, true) + C.fReset + " ");
                });

        _corePermission._permissionProfiles.forEach((Player target, PermissionProfile permissionProfile) ->
        {
            PermissionGroup highestGroup = PermissionGroup.getGroupWithHighestWeight(permissionProfile._groups());
            if (highestGroup == null)
            {
                return;
            }

            _hexusPlugin.getServer()
                    .getOnlinePlayers()
                    .stream()
                    .map(Player::getScoreboard)
                    .map(scoreboard -> scoreboard.getTeam(highestGroup._prefix))
                    .forEach(team -> team.addEntry(target.getName()));
        });
    }

}
