package net.hexuscraft.arcade.manager.command;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public final class CommandGameStop extends BaseCommand<ArcadeManager>
{

    CommandGameStop(final ArcadeManager arcadeManager)
    {
        super(arcadeManager,
              "stop",
              "",
              "Stop the currently active game.",
              Set.of(),
              ArcadeManager.PERM.COMMAND_GAME_STOP);
    }

}
