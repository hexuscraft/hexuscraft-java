package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.database.messages.ChatAnnouncementMessage;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandAnnouncement extends BaseCommand<CoreChat>
{

    CoreDatabase _coreDatabase;

    public CommandAnnouncement(CoreChat coreChat, CoreDatabase coreDatabase)
    {
        super(coreChat,
              "announce",
              "<Permission Group> <Message>",
              "Broadcast a message to the entire network.",
              Set.of("announcement"),
              CoreChat.PERM.COMMAND_ANNOUNCEMENT);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length < 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        PermissionGroup permissionGroup;
        try
        {
            permissionGroup = PermissionGroup.valueOf(args[0]);
        }
        catch (IllegalArgumentException ex)
        {
            sender.sendMessage(F.fMain(this,
                                       F.fItem(args[0]),
                                       " is not a valid group. Groups: ",
                                       F.fItem(PermissionGroup.getColoredNames())));
            return;
        }

        _coreDatabase._database._jedis.publish(ChatAnnouncementMessage.CHANNEL_NAME,
                                               new ChatAnnouncementMessage(sender instanceof Player player ?
                                                                           player.getUniqueId() :
                                                                           UtilUniqueId.EMPTY_UUID,
                                                                           String.join(" ",
                                                                                       Arrays.stream(args)
                                                                                             .skip(1)
                                                                                             .toArray(String[]::new)),
                                                                           permissionGroup).toString());

        sender.sendMessage(F.fMain(this, "Message has been announced."));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        }
        return List.of();
    }
}