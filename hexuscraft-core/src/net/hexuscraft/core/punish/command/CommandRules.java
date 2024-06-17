package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.punish.MiniPluginPunish;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandRules extends BaseCommand<MiniPluginPunish> {

    public CommandRules(final MiniPluginPunish plugin) {
        super(plugin, "rules", "", "View our server rules.", Set.of(), MiniPluginPunish.PERM.COMMAND_RULES);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Listing Rules:");
        sender.sendMessage(F.fMain("") + F.fList(1) + "Don't use gameplay-affecting client modifications");
        sender.sendMessage(F.fMain("") + F.fList(2) + "Don't be a dick");
    }

}
