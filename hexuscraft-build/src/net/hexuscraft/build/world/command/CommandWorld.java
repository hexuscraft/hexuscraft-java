package net.hexuscraft.build.world.command;

import net.hexuscraft.build.world.BuildWorld;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public final class CommandWorld extends BaseMultiCommand<BuildWorld> {

    public CommandWorld(final BuildWorld buildWorld) {
        super(buildWorld,
                "world",
                "Create, edit and remove worlds.",
                Set.of(),
                BuildWorld.PERM.COMMAND_WORLD,
                Set.of(
                        new CommandWorldCreate(buildWorld)
                ));
    }

}
