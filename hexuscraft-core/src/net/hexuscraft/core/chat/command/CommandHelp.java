package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class CommandHelp extends BaseCommand {

    public CommandHelp(PluginChat pluginChat) {
        super(pluginChat, "help", "", "Need some help? We got you covered.", Set.of("?"), PluginChat.PERM.COMMAND_HELP);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            sender.sendMessage(help(alias));
            return;
        }
        sender.sendMessage(F.fMain(this) + "Hey there! Need some help?");
        sender.sendMessage(F.fMain() + "Visit our website: " + C.cGreen + "www.hexuscraft.net");
        sender.sendMessage(F.fMain() + "Join our Discord: " + C.cPurple + "discord.hexuscraft.net");
        sender.sendMessage(F.fMain() + "Still stuck? Request help from staff with " + F.fItem("/support") + ".");
    }

}