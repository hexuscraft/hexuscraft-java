package net.hexuscraft.build.world.command;

import net.hexuscraft.build.Build;
import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public final class CommandWorld extends BaseMultiCommand<Build> {

    public CommandWorld(final MiniPlugin<Build> miniPlugin) {
        super(miniPlugin, "world", "Create, edit and remove worlds.", Set.of("w","wrld"), MiniPluginWorld.PERM.COMMAND_WORLD, Set.of(
                new CommandWorldCreate(miniPlugin)
        ));
    }

}
