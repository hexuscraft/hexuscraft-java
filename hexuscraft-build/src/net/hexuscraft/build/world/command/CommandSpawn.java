package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandSpawn extends BaseCommand<MiniPluginWorld> {

    public CommandSpawn(final MiniPluginWorld miniPluginWorld) {
        super(miniPluginWorld, "spawn", "", "Teleport to the spawn location", Set.of(),
                MiniPluginWorld.PERM.COMMAND_SPAWN);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command")));
            return;
        }

        player.teleport(_miniPlugin._hexusPlugin.getSpawn());
        player.sendMessage(F.fMain(this, "Teleported to the spawn location."));
    }

}
