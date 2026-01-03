package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandBroadcast extends BaseCommand<MiniPluginChat> {

    public CommandBroadcast(final MiniPluginChat miniPluginChat) {
        super(miniPluginChat, "s", "<Message>", "Broadcast a message to your current server.",
                Set.of("broadcast", "bc"), MiniPluginChat.PERM.COMMAND_BROADCAST);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            _miniPlugin._hexusPlugin.getServer().getOnlinePlayers()
                    .forEach(player -> player.playSound(player.getLocation(), Sound.NOTE_PLING, Integer.MAX_VALUE, 2));
            _miniPlugin._hexusPlugin.getServer().broadcastMessage(F.fMain(this,
                    F.fItem(sender instanceof final Player player ? player.getDisplayName() : sender.getName()), ": ",
                    C.cWhite + ChatColor.translateAlternateColorCodes('&', String.join(" ", args))));
            return;
        }
        sender.sendMessage(help(alias));
    }

}