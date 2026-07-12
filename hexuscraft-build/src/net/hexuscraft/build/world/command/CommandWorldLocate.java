package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandWorldLocate extends BaseCommand<BuildWorld> {

	public CommandWorldLocate(final BuildWorld buildWorld) {
		super(buildWorld, "locate", "<Player>", "Locate which world a player is in.", Set.of("find"), BuildWorld.PERM.COMMAND_WORLD_LOCATE);
	}

	@Override
	public void run(final CommandSender sender, final String alias, final String[] args) {
		final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender, players -> players.length != 1);
		if (targets.length != 1) return;

		sender.sendMessage(F.fMain(this, "Located ", F.fItem(targets[0].getName()), " in world ", F.fItem(targets[0].getWorld().getName()), "."));
	}

}
