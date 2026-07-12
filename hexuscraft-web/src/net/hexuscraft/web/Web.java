package net.hexuscraft.web;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.authentication.CoreTwoFactorAuthentication;
import net.hexuscraft.core.buildversion.CoreBuildVersion;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.featureflags.CoreFeatureFlags;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.store.CoreStore;
import net.hexuscraft.web.sales.WebSales;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Web extends HexusPlugin {

	public Web() {
		super(false);

		require(new CoreTwoFactorAuthentication(this));
		require(new CoreBuildVersion(this));
		require(new CoreChat(this));
		require(new CoreCommand(this));
		require(new CoreDatabase(this));
		require(new CoreFeatureFlags(this));
		require(new CorePermission(this));
		require(new CorePortal(this));
		require(new CoreStore(this));

		require(new WebSales(this));
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.teleport(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
		player.setGameMode(GameMode.SPECTATOR);
	}

	@EventHandler
	void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	void onPlayerMove(PlayerMoveEvent event) {
		event.setTo(new Location(getServer().getWorlds().getFirst(), 0, 0, 0, 0, 0));
	}

}
