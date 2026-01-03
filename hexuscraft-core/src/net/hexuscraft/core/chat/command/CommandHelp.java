package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandHelp extends BaseCommand<MiniPluginChat> {

    public CommandHelp(final MiniPluginChat miniPluginChat) {
        super(miniPluginChat, "help", "", "Need some help? We got you covered.", Set.of("?"),
                MiniPluginChat.PERM.COMMAND_HELP);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (args.length == 1) {

            return;
        }

        sender.sendMessage(F.fMain(this, "Hey there! Need some help?"));
        sender.sendMessage(F.fMain("", "Visit our website: " + C.cGreen + "https://hexuscraft.net"));
        sender.sendMessage(F.fMain("", "Join our Discord: " + C.cPurple + "https://discord.gg/yusJMxrg3e"));
        sender.sendMessage(F.fMain("", "Request help from staff with " + F.fItem("/support") + "."));
        sender.sendMessage(F.fMain("", "Type ", F.fItem("/" + alias + " 1"), " for a list of commands."));
    }

}