package net.hexuscraft.core.authentication;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginAuth extends MiniPlugin {

    public enum PERM implements IPermission {
        REQUIRE_AUTHENTICATION
    }

    public PluginAuth(JavaPlugin plugin) {
        super(plugin, "Authentication");

        PermissionGroup.TRAINEE._permissions.add(PERM.REQUIRE_AUTHENTICATION);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(PERM.REQUIRE_AUTHENTICATION.name())) {
            return;
        }
        event.getPlayer().sendMessage(F.fMain(this) + "You are authenticated.");
    }

}