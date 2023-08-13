package net.hexuscraft.hub.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.hub.entity.PluginEntity;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class CommandEntityPurge extends BaseCommand {

    PluginEntity pluginEntity;

    CommandEntityPurge(PluginEntity pluginEntity) {
        super(pluginEntity, "purge", "", "Temporarily purge all NPCs.", Set.of("kill", "p"), PluginEntity.PERM.COMMAND_ENTITY_PURGE);
        this.pluginEntity = pluginEntity;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        int amount = pluginEntity.purge();

        sender.sendMessage(F.fMain(this) + "Temporarily purged " + F.fElem(Integer.toString(amount) + " NPCs") + ".");
    }

}
