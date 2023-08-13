package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.punish.PluginPunish;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandPunishHistory extends BaseCommand {

    public CommandPunishHistory(PluginPunish pluginPunish) {
        super(pluginPunish, "punishhistory", "", "View your history of punishments.", Set.of("ph"), PluginPunish.PERM.COMMAND_PUNISH_HISTORY);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        sender.sendMessage(F.fMain(this) + "Not implemented.");
    }
}
