package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandSupportResponse extends BaseCommand<CoreChat>
{

    CorePermission _corePermission;

    public CommandSupportResponse(CoreChat coreChat, CorePermission corePermission)
    {
        super(coreChat,
              "supportresponse",
              "<Player> <Message>",
              "Respond to a help request.",
              Set.of("ma", "sr"),
              CoreChat.PERM.COMMAND_SUPPORT_STAFF);
        _corePermission = corePermission;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 1)
        {
            Player target = _miniPlugin._hexusPlugin.getServer().getPlayer(args[0]);
            if (target == null)
            {
                sender.sendMessage(F.fMain(this, "Could not find a player with specified name."));
                return;
            }

            PermissionGroup permissionGroup = sender instanceof Player ?
                                              PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(
                                                      (Player) sender)._groups()) :
                                              null;

            for (Player player : _miniPlugin._hexusPlugin.getServer().getOnlinePlayers())
            {
                if (player.equals(sender) ||
                    player.equals(target) ||
                    player.hasPermission(PermissionGroup.TRAINEE.name()))
                {
                    PermissionGroup
                            targetGroup
                            = PermissionGroup.getGroupWithHighestWeight(_corePermission._permissionProfiles.get(player)
                                                                                                           ._groups());

                    String sourceStr = F.fPermissionGroup(permissionGroup) + " " + sender.getName();
                    String targetStr = F.fPermissionGroup(targetGroup) + " " + player.getName();

                    player.sendMessage(sourceStr +
                                       C.fReset +
                                       C.cPurple +
                                       " -> " +
                                       C.fReset +
                                       targetStr +
                                       C.fReset +
                                       " " +
                                       C.cPurple +
                                       String.join(" ", Arrays.stream(args).toList().subList(1, args.length)));
                    if (player.equals(target))
                    {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                    }
                }
            }

            return;
        }

        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 1)
        {
            return List.of();
        }
        return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                                                    sender,
                                                    false);
    }

}
