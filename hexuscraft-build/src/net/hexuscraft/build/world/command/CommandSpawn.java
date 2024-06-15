package net.hexuscraft.build.world.command;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandSpawn extends BaseCommand<Build> {

    public CommandSpawn(final MiniPlugin<Build> miniPlugin) {
        super(miniPlugin, "spawn", "", "Teleport to the spawn location", Set.of(), MiniPluginWorld.PERM.COMMAND_SPAWN);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command")));
            return;
        }

        player.teleport(_miniPlugin._plugin.getSpawn());
    }

}
