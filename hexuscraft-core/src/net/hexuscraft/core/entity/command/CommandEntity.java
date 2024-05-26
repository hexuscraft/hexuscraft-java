package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.entity.PluginEntity;

import java.util.Set;

public class CommandEntity extends BaseMultiCommand<HexusPlugin> {

    public CommandEntity(PluginEntity pluginEntity) {
        super(pluginEntity, "entity", "Manage server entities.", Set.of("npc", "mob"), PluginEntity.PERM.COMMAND_ENTITY, Set.of(
                new CommandEntityList(pluginEntity),
                new CommandEntityRefresh(pluginEntity),
                new CommandEntityPurge(pluginEntity)
        ));
    }

}
