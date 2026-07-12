package net.hexuscraft.arcade.host.command;

import net.hexuscraft.arcade.host.ArcadeHost;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.player.UtilTitleTab;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class CommandHostSet extends BaseCommand<ArcadeHost> {
	public CommandHostSet(ArcadeHost arcadeHost) {
		super(arcadeHost, "set", "[Player]", "View or set the host of this server.", Set.of("s"), ArcadeHost.PERM.COMMAND_HOST_SET);
	}

	@Override
	public void run(CommandSender sender, String alias, String[] args) {
		if (args.length > 1) {
			sender.sendMessage(help(alias));
			return;
		}

		_miniPlugin._hexusPlugin.runAsync(() -> {
			String message;
			if (args.length == 0) {
				if (_miniPlugin._host == null) {
					sender.sendMessage(F.fMain(this, F.fError("This server already has no host.")));
					if (sender instanceof Player player) {
						player.playSound(player.getLocation(), Sound.NOTE_BASS, Float.MAX_VALUE, 1);
					}
					return;
				}

				_miniPlugin._host = null;
				message = "There is no longer a server host";
			} else {
				_miniPlugin._host = PlayerSearch.offlinePlayerSearch(args[0], sender);
				if (_miniPlugin._host == null) return;
				message = _miniPlugin._host.getName() + " is now the server host";
			}

			_miniPlugin.refreshHostPermissions();
			_miniPlugin._hexusPlugin.getServer().broadcastMessage(F.fMain("Server Host", message));
			_miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
				UtilTitleTab.sendSubtitle(player, message, 20, 100, 20);
				UtilTitleTab.sendTitle(player, C.cAqua + "Server Host", 20, 100, 20);
				player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
			});
		});
	}

	@Override
	public List<String> tab(CommandSender sender, String alias, String[] args) {
		if (args.length == 1) {
			return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender, false);
		}
		return List.of();
	}
}
