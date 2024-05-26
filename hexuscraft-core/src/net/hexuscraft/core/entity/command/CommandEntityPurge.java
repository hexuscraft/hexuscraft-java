package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.PluginEntity;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandEntityPurge extends BaseCommand<HexusPlugin> {

    final PluginEntity pluginEntity;

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

        sender.sendMessage(F.fMain(this) + "Temporarily purged " + F.fItem(amount + " NPCs") + ".");
    }

}
