package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandBroadcast extends BaseCommand<MiniPluginChat> {

    public CommandBroadcast(final MiniPluginChat miniPluginChat) {
        super(miniPluginChat, "s", "<Message>", "Broadcast a server message.", Set.of("broadcast", "bc"), MiniPluginChat.PERM.COMMAND_BROADCAST);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            _miniPlugin._hexusPlugin.getServer().broadcastMessage(F.fMain(this, F.fItem(sender), ": ", C.cWhite + ChatColor.translateAlternateColorCodes('&', String.join(" ", args))));
            return;
        }
        sender.sendMessage(help(alias));
    }

}