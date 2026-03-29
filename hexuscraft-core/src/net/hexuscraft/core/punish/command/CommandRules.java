package net.hexuscraft.core.punish.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.punish.CorePunish;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandRules extends BaseCommand<CorePunish>
{

    public CommandRules(CorePunish plugin)
    {
        super(plugin, "rules", "", "View our server rules.", Set.of(), CorePunish.PERM.COMMAND_RULES);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this, "Server Rules:") +
                "\n" +
                F.fMain("", "1. Don't use gameplay-affecting client modifications") +
                "\n" +
                F.fMain("", "2. Be respectful, civil, and kind to all players"));
    }

}
