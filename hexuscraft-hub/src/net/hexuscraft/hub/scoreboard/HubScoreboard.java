package net.hexuscraft.hub.scoreboard;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.scoreboard.CoreScoreboard;
import net.hexuscraft.core.scoreboard.CustomScoreboard;
import net.hexuscraft.hub.Hub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HubScoreboard extends MiniPlugin<Hub> {

	final Map<Player, BukkitTask> _sidebarUpdateTasks;
	private final String SIDEBAR_TITLE = "          Welcome %s, to the Hexuscraft Network!";
	CorePortal _corePortal;
	CorePermission _corePermission;
	CoreScoreboard _coreScoreboard;

	public HubScoreboard(Hub hub) {
		super(hub, "Hub Scoreboard");

		_sidebarUpdateTasks = new HashMap<>();
	}

	@Override
	public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
		_corePortal = (CorePortal) dependencies.get(CorePortal.class);
		_corePermission = (CorePermission) dependencies.get(CorePermission.class);
		_coreScoreboard = (CoreScoreboard) dependencies.get(CoreScoreboard.class);
	}

	@Override
	public void onEnable() {
		_hexusPlugin.getServer().getOnlinePlayers().stream().map(player -> new PlayerJoinEvent(player, null)).forEach(this::onPlayerJoin);
	}

	@Override
	public void onDisable() {
		_sidebarUpdateTasks.values().forEach(BukkitTask::cancel);
		_sidebarUpdateTasks.clear();
	}

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CustomScoreboard customScoreboard = _coreScoreboard._customScoreboards.get(player);
		customScoreboard._sidebar.setTitle(SIDEBAR_TITLE.formatted(player.getName()));
		_sidebarUpdateTasks.put(player, _hexusPlugin.runSyncTimer(() -> customScoreboard._sidebar.setLines(generateSidebarLines(player)), 0, 20));
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		Optional.ofNullable(_sidebarUpdateTasks.remove(event.getPlayer())).ifPresent(BukkitTask::cancel);
	}

	String[] generateSidebarLines(Player player) {
		PermissionGroup rank = PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(player)._groups());

		return new String[]{"", " " + C.cYellow + C.fBold + player.getName(), "  Rank: " + F.fPermissionGroup(rank), "  Level: " + C.cYellow + "0 (▲ 0%)", "  Coins: " + C.cYellow + "0", "  Completion: " + C.cYellow + "0%", "", " " + C.cGreen + C.fBold + _corePortal._serverName, "  Players: " + C.cGreen + _hexusPlugin.getServer().getOnlinePlayers().size() + "/" + _hexusPlugin.getServer().getMaxPlayers(), "", " " + C.cGold + C.fBold + "Hexuscraft", "  Players: " + C.cGold + Arrays.stream(_corePortal.getServers()).mapToInt(s -> s._players).sum(), "", C.cGray + "www.hexuscraft.net",};
	}

}
