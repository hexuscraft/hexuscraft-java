package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandBroadcast extends BaseCommand<CoreChat>
{

    public CommandBroadcast(CoreChat coreChat)
    {
        super(coreChat,
                "s",
                "<Message>",
                "Broadcast a message to your current server.",
                Set.of("broadcast", "bc"),
                CoreChat.PERM.COMMAND_BROADCAST);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));

        _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player ->
        {
            //noinspection deprecation
            player.sendTitle(C.cYellow + "Broadcast", message);
            player.sendMessage(F.fMain("Broadcast", C.cAqua + message));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
        });
    }
}
