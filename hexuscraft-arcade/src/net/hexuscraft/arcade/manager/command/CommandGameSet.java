package net.hexuscraft.arcade.manager.command;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.core.command.BaseCommand;

import java.util.Set;

public class CommandGameSet extends BaseCommand<ArcadeManager>
{

    CommandGameSet(ArcadeManager arcadeManager)
    {
        super(arcadeManager,
              "set",
              "<Name> [Map]",
              "Change the active game.",
              Set.of("change", "c"),
              ArcadeManager.PERM.COMMAND_GAME_SET);
    }

}
