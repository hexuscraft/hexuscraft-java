package net.hexuscraft.core.npc.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.npc.CoreNpc;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandNpcRefresh extends BaseCommand<CoreNpc>
{

    CommandNpcRefresh(CoreNpc coreNpc)
    {
        super(coreNpc, "refresh", "", "Refresh all NPCs.", Set.of("ref", "r"), CoreNpc.PERM.COMMAND_ENTITY_REFRESH);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        long start = System.currentTimeMillis();

        sender.sendMessage(F.fMain(this) + "Processing NPC refresh...");
        _miniPlugin._hexusPlugin.getServer().getWorlds().forEach(world ->
        {
            sender.sendMessage(F.fMain(this) + "Refreshing NPCs in world " + F.fItem(world.getName()) + ".");
            _miniPlugin.refreshNPCs(world);
        });

        sender.sendMessage(F.fMain(this) +
                "Completed NPC refresh in " +
                F.fItem(F.fTime(System.currentTimeMillis() - start)) +
                ".");
    }

}
