package net.hexuscraft.core.authentication;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class CoreAuthentication extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        REQUIRE_AUTHENTICATION
    }

    public CoreAuthentication(HexusPlugin plugin)
    {
        super(plugin, "Authentication");

        PermissionGroup.TRAINEE._permissions.add(PERM.REQUIRE_AUTHENTICATION);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!event.getPlayer().hasPermission(PERM.REQUIRE_AUTHENTICATION.name()))
        {
        }
        // TOOD: 2fa
    }

}