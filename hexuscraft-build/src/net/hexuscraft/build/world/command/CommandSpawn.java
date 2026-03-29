package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandSpawn extends BaseCommand<BuildWorld>
{

    public CommandSpawn(BuildWorld buildWorld)
    {
        super(buildWorld, "spawn", "", "Teleport to the spawn location", Set.of(), BuildWorld.PERM.COMMAND_SPAWN);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command")));
            return;
        }

        player.teleport(_miniPlugin._hexusPlugin.getSpawn());
        player.sendMessage(F.fMain(this, "Teleported to the spawn location."));
    }

}
