package net.hexuscraft.core.npc.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.FBukkit;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.npc.CoreNpc;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandNpcList extends BaseCommand<CoreNpc>
{

    CommandNpcList(CoreNpc coreNpc)
    {
        super(coreNpc, "list", "", "List all NPCs.", Set.of("ls", "l"), CoreNpc.PERM.COMMAND_ENTITY_LIST);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        Entity[] entities = _miniPlugin.list();

        List<String> response = new ArrayList<>();
        response.add(F.fMain(this, "Listing ", F.fItem(entities.length + " Entities")));
        response.addAll(Arrays.stream(entities)
                .map(entity -> F.fMain("",
                        F.fItem(entity.getCustomName()),
                        " (",
                        entity.getType().name(),
                        ") (",
                        FBukkit.fItem(entity.getLocation()),
                        ")"))
                .toList());
        sender.sendMessage(String.join("\n", response));
    }

}
