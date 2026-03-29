package net.hexuscraft.arcade.manager.command;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public class CommandGameStart extends BaseCommand<ArcadeManager>
{

    CommandGameStart(ArcadeManager arcadeManager)
    {
        super(arcadeManager,
                "start",
                "[Seconds]",
                "Start the currently loaded game.",
                Set.of(),
                ArcadeManager.PERM.COMMAND_GAME_START);
    }

}
