package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.PluginEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Set;

public class CommandEntityList extends BaseCommand {

    final PluginEntity pluginEntity;

    CommandEntityList(PluginEntity pluginEntity) {
        super(pluginEntity, "list", "", "List all NPCs.", Set.of("ls", "l"), PluginEntity.PERM.COMMAND_ENTITY_LIST);
        this.pluginEntity = pluginEntity;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        Entity[] entities = pluginEntity.list();

        sender.sendMessage(F.fMain(this) + "Listing entities:");

        Arrays.stream(entities).toList().forEach(entity -> sender.sendMessage(F.fMain("") + entity.getName() + " [" + entity.getLocation().toString() + "]"));

        sender.sendMessage(F.fMain(this) + "Listed " + F.fItem(entities.length + " Entities") + ".");

    }

}
