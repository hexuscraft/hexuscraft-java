package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandWorldList extends BaseCommand<BuildWorld> {

	public CommandWorldList(BuildWorld buildWorld) {
		super(buildWorld, "list", "", "See a list of existing worlds.", Set.of("l"), BuildWorld.PERM.COMMAND_WORLD_LIST);
	}

	@Override
	public void run(CommandSender sender, String alias, String[] args) {
		sender.sendMessage(F.fMain(this, "Listing Worlds: ", F.fItem(_miniPlugin._hexusPlugin.getServer().getWorlds().stream().map(World::getName).toArray(String[]::new))));
	}

}
