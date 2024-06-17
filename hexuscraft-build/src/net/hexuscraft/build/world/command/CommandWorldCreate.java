package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandWorldCreate extends BaseCommand<MiniPluginWorld> {

    public CommandWorldCreate(final MiniPluginWorld miniPluginWorld) {
        super(miniPluginWorld, "create", "<Name> <Type> <Seed>", "Create a new world.", Set.of("c"), MiniPluginWorld.PERM.COMMAND_WORLD_CREATE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        sender.sendMessage("wip");
    }

}
