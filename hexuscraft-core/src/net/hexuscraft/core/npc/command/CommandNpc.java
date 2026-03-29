package net.hexuscraft.core.npc.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.npc.CoreNpc;

import java.util.Set;

public final class CommandNpc extends BaseMultiCommand<CoreNpc>
{

    public CommandNpc(final CoreNpc coreNpc)
    {
        super(coreNpc,
              "npc",
              "Manage server NPCs.",
              Set.of("nonplayercharacter"),
              CoreNpc.PERM.COMMAND_ENTITY,
              Set.of(new CommandNpcList(coreNpc), new CommandNpcRefresh(coreNpc), new CommandNpcPurge(coreNpc)));
    }

}
