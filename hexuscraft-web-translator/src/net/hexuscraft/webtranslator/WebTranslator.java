package net.hexuscraft.webtranslator;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class WebTranslator extends HexusPlugin {

    public WebTranslator() {
        super(false);

        require(MiniPluginCommand.class);
        require(MiniPluginDatabase.class);
        require(MiniPluginPermission.class);
        require(MiniPluginPortal.class);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.setJoinMessage(F.fSub("Join",
                player.getName()));
        player.teleport(new Location(getServer().getWorlds()
                .getFirst(),
                0,
                0,
                0,
                0,
                0));
        player.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        event.setQuitMessage(F.fSub("Quit",
                event.getPlayer()
                        .getName()));
    }

    @EventHandler
    private void onEntityDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerMove(final PlayerMoveEvent event) {
        event.setTo(new Location(getServer().getWorlds()
                .getFirst(),
                0,
                0,
                0,
                0,
                0));
    }

}
