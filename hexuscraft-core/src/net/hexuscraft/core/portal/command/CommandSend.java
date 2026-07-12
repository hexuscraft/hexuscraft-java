package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.actionbar.ActionBar;
import net.hexuscraft.core.actionbar.CoreActionBar;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandSend extends BaseCommand<CorePortal> {

	CoreActionBar _coreActionBar;

	public CommandSend(CorePortal corePortal, CoreActionBar coreActionBar) {
		super(corePortal, "send", "<Player> <Name>", "Teleport a player to a server.", Set.of(), CorePortal.PERM.COMMAND_SEND);

		_coreActionBar = coreActionBar;
	}

	@Override
	public void run(CommandSender sender, String alias, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(help(alias));
			return;
		}

		sender.sendMessage(F.fMain(this, "Attempting to send player ", F.fItem(args[0]), " to server ", F.fItem(args[1]), "..."));

		ActionBar loadingBar;
		if (sender instanceof final Player player)
			loadingBar = _coreActionBar.registerActionBar(new ActionBar(_coreActionBar, player, 0, F.fActionBar(this, "Attempting to send player ", F.fItem(args[0]), " to server ", F.fItem(args[1]), "...")));
		else loadingBar = null;

		OfflinePlayer target = PlayerSearch.offlinePlayerSearch(args[0], sender);
		if (target == null) return;

		sender.sendMessage(F.fMain(this, "Found offline player ", F.fItem(args[0]), "..."));

		if (loadingBar != null)
			loadingBar.setMessage(F.fActionBar(this, "Attempting to send player ", F.fItem(target.getName()), " to server ", F.fItem(args[1]), "..."));

		ServerData server = _miniPlugin.getServer(args[1]);
		if (server == null) {
			sender.sendMessage(F.fMain(this, F.fError("Could not locate server with name ", F.fItem(args[1]), ".")));
			if (loadingBar != null) _coreActionBar.unregisterActionBar(loadingBar);
			return;
		}

		sender.sendMessage(F.fMain(this, "Found server ", F.fItem(args[1]), "..."));

		if (loadingBar != null)
			loadingBar.setMessage(F.fActionBar(this, "Sending player ", F.fItem(target.getName()), " to server ", F.fItem(server._name), "..."));

		_miniPlugin._hexusPlugin.runAsync(() -> {
			try {
				_miniPlugin.teleportAsync(target.getUniqueId(), server._name, sender instanceof final Player player ? player.getUniqueId() : null);
				sender.sendMessage(F.fMain(this, F.fSuccess("Sent player ", F.fItem(target.getName()), " to server ", F.fItem(server._name), ".")));
				if (loadingBar != null)
					loadingBar.setMessage(F.fActionBar(this, F.fSuccess("Sent player ", F.fItem(target.getName()), " to server ", F.fItem(server._name), ".")));
			} catch (JedisException ex) {
				_miniPlugin.logSevere(ex);
				sender.sendMessage(F.fMain(this, F.fError("Error while sending player ", F.fItem(target.getName()), " to server ", F.fItem(server._name), ".")));
				if (loadingBar != null)
					loadingBar.setMessage(F.fActionBar(this, F.fError("Error while sending player ", F.fItem(target.getName()), " to server ", F.fItem(server._name), ".")));
			} finally {
				if (loadingBar != null)
					_coreActionBar.unregisterActionBar(loadingBar);
			}
		});
	}

	@Override
	public List<String> tab(CommandSender sender, String alias, String[] args) {
		if (args.length == 1) {
			return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender, false);
		}
		if (args.length == 2) {
			return Arrays.asList(_miniPlugin.getServerNames());
		}
		return List.of();
	}

}
