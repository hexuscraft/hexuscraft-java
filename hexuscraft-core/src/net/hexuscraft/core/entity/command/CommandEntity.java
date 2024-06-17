package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.entity.MiniPluginEntity;

import java.util.Set;

public final class CommandEntity extends BaseMultiCommand<MiniPluginEntity> {

    public CommandEntity(final MiniPluginEntity miniPluginEntity) {
        super(miniPluginEntity, "entity", "Manage server entities.", Set.of("npc", "mob"), MiniPluginEntity.PERM.COMMAND_ENTITY, Set.of(
                new CommandEntityList(miniPluginEntity),
                new CommandEntityRefresh(miniPluginEntity),
                new CommandEntityPurge(miniPluginEntity)
        ));
    }

}
