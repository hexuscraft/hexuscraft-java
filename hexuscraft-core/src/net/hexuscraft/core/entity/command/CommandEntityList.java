package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.MiniPluginEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Set;

public final class CommandEntityList extends BaseCommand<MiniPluginEntity> {

    CommandEntityList(final MiniPluginEntity miniPluginEntity) {
        super(miniPluginEntity, "list", "", "List all NPCs.", Set.of("ls", "l"), MiniPluginEntity.PERM.COMMAND_ENTITY_LIST);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        Entity[] entities = _miniPlugin.list();

        sender.sendMessage(F.fMain(this) + "Listing entities:");

        Arrays.stream(entities).toList().forEach(entity -> sender.sendMessage(F.fMain("") + entity.getName() + " [" + entity.getLocation().toString() + "]"));

        sender.sendMessage(F.fMain(this) + "Listed " + F.fItem(entities.length + " Entities") + ".");

    }

}
