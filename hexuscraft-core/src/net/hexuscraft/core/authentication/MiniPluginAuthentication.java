package net.hexuscraft.core.authentication;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public final class MiniPluginAuthentication extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        REQUIRE_AUTHENTICATION
    }

    public MiniPluginAuthentication(final HexusPlugin plugin) {
        super(plugin, "Authentication");

        PermissionGroup.TRAINEE._permissions.add(PERM.REQUIRE_AUTHENTICATION);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(PERM.REQUIRE_AUTHENTICATION.name())) {
            return;
        }
        event.getPlayer().sendMessage(F.fMain(this) + "You are authenticated.");
    }

}