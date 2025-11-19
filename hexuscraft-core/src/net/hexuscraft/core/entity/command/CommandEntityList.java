package net.hexuscraft.core.entity.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.entity.MiniPluginEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        final Entity[] entities = _miniPlugin.list();

        final List<String> response = new ArrayList<>();
        response.add(F.fMain(this, "Listing ", F.fItem(String.valueOf(entities.length)), " entities:"));
        response.addAll(Arrays.stream(entities).map(entity -> F.fMain("", F.fItem(entity.getCustomName()), " (", entity.getType().name(), ") (", F.fItem(entity.getLocation()), ")")).toList());
        sender.sendMessage(String.join("\n", response));
    }

}
