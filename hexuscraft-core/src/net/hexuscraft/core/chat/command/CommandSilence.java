package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public final class CommandSilence extends BaseCommand<MiniPluginChat> {

    public CommandSilence(final MiniPluginChat miniPluginChat) {
        super(miniPluginChat, "silence", "", "Mute the global chat.", Set.of("mutechat"),
                MiniPluginChat.PERM.COMMAND_SILENCE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 0) {
            if (_miniPlugin.getMuted()) {
                _miniPlugin.setMuted(false, true);
                _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (!player.hasPermission(MiniPluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;

                    JSONObject jsonObject = new JSONObject(Map.of("text",
                            F.fStaff() + F.fMain(this, F.fSuccess(
                                    F.fItem(sender instanceof final Player senderPlayer ? player.getDisplayName() :
                                            sender.getName()), " un-muted the global chat."))));
                    player.sendRawMessage(jsonObject.toString());
                });
                return;
            }
            _miniPlugin.setMuted(true, true);
            _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player -> {
                if (!player.hasPermission(MiniPluginChat.PERM.COMMAND_SILENCE_SEE.name())) return;
                player.sendMessage(F.fStaff() + F.fMain(this, F.fError(
                        F.fItem(sender instanceof final Player senderPlayer ? player.getDisplayName() : sender.getName()),
                        " muted the global chat")));
            });

            return;
        }
        sender.sendMessage(help(alias));
    }

}
