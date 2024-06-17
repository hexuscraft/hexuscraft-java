package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandSilence extends BaseCommand<MiniPluginChat> {

    public CommandSilence(final MiniPluginChat miniPluginChat) {
        super(miniPluginChat, "silence", "", "Mute the global chat.", Set.of("mutechat"), MiniPluginChat.PERM.COMMAND_SILENCE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0) {
            if (_miniPlugin.getMuted()) {
                _miniPlugin.setMuted(false, true);
                _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (!player.hasPermission(MiniPluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;
                    _miniPlugin._hexusPlugin.getServer().broadcastMessage(F.fSub("Staff", F.fItem(sender), " ", F.fSuccess("un-muted the global chat"), "."));
                });
                return;
            }
            _miniPlugin.setMuted(true, true);
            _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                if (!player.hasPermission(MiniPluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;
                player.sendMessage(F.fSub("Staff", F.fItem(sender), " ", F.fError("muted the global chat"), "."));
            });

            return;
        }
        sender.sendMessage(help(alias));
    }

}
