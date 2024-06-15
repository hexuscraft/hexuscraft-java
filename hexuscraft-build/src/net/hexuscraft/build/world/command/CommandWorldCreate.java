package net.hexuscraft.build.world.command;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public final class CommandWorldCreate extends BaseCommand<Build> {

    public CommandWorldCreate(final MiniPlugin<Build> miniPlugin) {
        super(miniPlugin, "create", "<Name> <Type> <Seed>", "Create a new world.", Set.of("c"), MiniPluginWorld.PERM.COMMAND_WORLD_CREATE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {

    }

}
