package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;

public final class CommandWorldTeleport extends BaseCommand<BuildWorld> {

	public CommandWorldTeleport(final BuildWorld buildWorld) {
		super(buildWorld, "teleport", "[Players] <World>", "Teleport players to an existing world.", Set.of("c"), BuildWorld.PERM.COMMAND_WORLD_TELEPORT);
	}

	@Override
	public void run(final CommandSender sender, final String alias, final String[] args) {
		final Player[] targets;
		if (args.length == 1) {
			if (!(sender instanceof final Player player)) {
				sender.sendMessage(F.fMain(this, F.fError("Only players can teleport themselves to a world.")));
				return;
			}
			targets = new Player[]{player};
		} else if (args.length == 2) {
			targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender, (final Player[] players) -> players.length == 0);
			if (targets.length == 0) return;
		} else {
			sender.sendMessage(help(alias));
			return;
		}

		final World world = _miniPlugin._hexusPlugin.getServer().getWorld(args[0]);
		if (world == null) {
			sender.sendMessage(F.fMain(this, F.fError("Could not locate world with name ", F.fItem(args[0]), ".")));
			return;
		}

		final Location spawn = world.getSpawnLocation();
		Arrays.stream(targets).forEach((final Player target) -> target.teleport(spawn));
	}

}
