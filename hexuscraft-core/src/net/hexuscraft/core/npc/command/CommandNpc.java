package net.hexuscraft.core.npc.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.npc.MiniPluginNpc;

import java.util.Set;

public final class CommandNpc extends BaseMultiCommand<MiniPluginNpc> {

    public CommandNpc(final MiniPluginNpc miniPluginNpc) {
        super(miniPluginNpc, "npc", "Manage server NPCs.", Set.of("nonplayercharacter"),
                MiniPluginNpc.PERM.COMMAND_ENTITY, Set.of(
                        new CommandNpcList(miniPluginNpc),
                        new CommandNpcRefresh(miniPluginNpc),
                        new CommandNpcPurge(miniPluginNpc)
                ));
    }

}
