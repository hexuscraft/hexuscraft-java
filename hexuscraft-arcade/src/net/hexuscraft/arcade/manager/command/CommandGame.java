package net.hexuscraft.arcade.manager.command;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.core.command.BaseMultiCommand;

import java.util.Set;

public class CommandGame extends BaseMultiCommand<ArcadeManager>
{

    public CommandGame(ArcadeManager arcadeManager)
    {
        super(arcadeManager,
              "game",
              "Manage the active game.",
              Set.of("arcade"),
              ArcadeManager.PERM.COMMAND_GAME,
              Set.of(new CommandGameSet(arcadeManager),
                     new CommandGameStart(arcadeManager),
                     new CommandGameStop(arcadeManager)));
    }

}
