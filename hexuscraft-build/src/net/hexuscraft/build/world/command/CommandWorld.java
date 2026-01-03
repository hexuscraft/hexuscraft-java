package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.MiniPluginWorld;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public final class CommandWorld extends BaseMultiCommand<MiniPluginWorld> {

    public CommandWorld(final MiniPluginWorld miniPluginWorld) {
        super(miniPluginWorld, "world", "Create, edit and remove worlds.", Set.of(), MiniPluginWorld.PERM.COMMAND_WORLD,
                Set.of(
                        new CommandWorldCreate(miniPluginWorld)
                ));
    }

}
