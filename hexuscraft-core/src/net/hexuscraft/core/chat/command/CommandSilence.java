package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandSilence extends BaseCommand {

    private final PluginChat _pluginChat;

    public CommandSilence(PluginChat pluginChat) {
        super(pluginChat, "silence", "", "Mute the global chat.", Set.of("mutechat"), PluginChat.PERM.COMMAND_SILENCE);

        _pluginChat = pluginChat;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length == 0) {
            if (_pluginChat.getMuted()) {
                _pluginChat.setMuted(false, true);
                _pluginChat._javaPlugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (!player.hasPermission(PluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;
                    _pluginChat._javaPlugin.getServer().broadcastMessage(F.fSub("Staff", F.fItem(sender), " ", F.fSuccess("un-muted the global chat"), "."));
                });
                return;
            }
            _pluginChat.setMuted(true, true);
            _pluginChat._javaPlugin.getServer().getOnlinePlayers().forEach(player -> {
                if (!player.hasPermission(PluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;
                player.sendMessage(F.fSub("Staff", F.fItem(sender), " ", F.fError("muted the global chat"), "."));
            });

            return;
        }
        sender.sendMessage(help(alias));
    }

}
