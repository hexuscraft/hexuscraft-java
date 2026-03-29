package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class CommandSilence extends BaseCommand<CoreChat>
{

    public CommandSilence(CoreChat coreChat)
    {
        super(coreChat, "silence", "", "Mute the global chat.", Set.of("mutechat"), CoreChat.PERM.COMMAND_SILENCE);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 0)
        {
            if (_miniPlugin.getMuted())
            {
                _miniPlugin.setMuted(false);
                _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player ->
                                                                                {
                                                                                    if (!player.hasPermission(CoreChat.PERM.COMMAND_SILENCE_SEE.name()))
                                                                                    {
                                                                                        return;
                                                                                    }

                                                                                    JSONObject
                                                                                            jsonObject
                                                                                            = new JSONObject(Map.of(
                                                                                            "text",
                                                                                            F.fStaff(this,
                                                                                                     F.fSuccess(F.fItem(
                                                                                                                        sender instanceof Player senderPlayer ?
                                                                                                                        senderPlayer.getDisplayName() :
                                                                                                                        sender.getName()),
                                                                                                                " un-muted the global chat."))));
                                                                                    player.sendRawMessage(jsonObject.toString());
                                                                                });
                return;
            }
            _miniPlugin.setMuted(true);
            _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(player ->
                                                                            {
                                                                                if (!player.hasPermission(CoreChat.PERM.COMMAND_SILENCE_SEE.name()))
                                                                                {
                                                                                    return;
                                                                                }
                                                                                player.sendMessage(F.fStaff(this,
                                                                                                            F.fError(F.fItem(
                                                                                                                             sender instanceof Player senderPlayer ?
                                                                                                                             senderPlayer.getDisplayName() :
                                                                                                                             sender.getName()),
                                                                                                                     " muted the global chat")));
                                                                            });

            return;
        }
        sender.sendMessage(help(alias));
    }

}
