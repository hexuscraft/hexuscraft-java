package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.MiniPluginEntity;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandEntityPurge extends BaseCommand<MiniPluginEntity> {

    CommandEntityPurge(final MiniPluginEntity miniPluginEntity) {
        super(miniPluginEntity, "purge", "", "Temporarily purge all NPCs.", Set.of("kill", "p"), MiniPluginEntity.PERM.COMMAND_ENTITY_PURGE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        int amount = _miniPlugin.purge();

        sender.sendMessage(F.fMain(this) + "Temporarily purged " + F.fItem(amount + " NPCs") + ".");
    }

}
