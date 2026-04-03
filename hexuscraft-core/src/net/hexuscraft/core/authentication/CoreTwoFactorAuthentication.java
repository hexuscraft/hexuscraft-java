package net.hexuscraft.core.authentication;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class CoreTwoFactorAuthentication extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        REQUIRE_AUTHENTICATION
    }

    public CoreTwoFactorAuthentication(HexusPlugin plugin)
    {
        super(plugin, "2FA");

        PermissionGroup.TRAINEE._permissions.add(PERM.REQUIRE_AUTHENTICATION);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        // TODO: 2fa
        if (!event.getPlayer().hasPermission(PERM.REQUIRE_AUTHENTICATION.name()))
        {
            return;
        }
        event.getPlayer().sendMessage(F.fMain(this, "Authenticated!"));
    }

}