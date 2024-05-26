package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.punish.PluginPunish;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandRules extends BaseCommand<HexusPlugin> {

    public CommandRules(final MiniPlugin<HexusPlugin> plugin) {
        super(plugin, "rules", "", "View our server rules.", Set.of(), PluginPunish.PERM.COMMAND_RULES);
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Listing Rules:");
        sender.sendMessage(F.fMain("") + F.fList(1) + "Don't use gameplay-affecting client modifications");
        sender.sendMessage(F.fMain("") + F.fList(2) + "Don't be a dick");
    }

}
