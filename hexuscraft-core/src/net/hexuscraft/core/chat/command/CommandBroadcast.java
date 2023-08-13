package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandBroadcast extends BaseCommand {

    public CommandBroadcast(PluginChat pluginChat) {
        super(pluginChat, "s", "<Message>", "Broadcast a server message.", Set.of("broadcast", "bc"), PluginChat.PERM.COMMAND_BROADCAST);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            _miniPlugin._javaPlugin.getServer().broadcastMessage(F.fBroadcast(sender, String.join(" ", args)));
            return;
        }
        sender.sendMessage(help(alias));
    }

}