package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.database.messages.ChatSupportMessage;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public class CommandSupport extends BaseCommand<CoreChat>
{

    private final CorePortal _corePortal;
    private final CorePermission _corePermission;
    private final CoreDatabase _coreDatabase;

    public CommandSupport(CoreChat coreChat,
                          CorePermission corePermission,
                          CoreDatabase coreDatabase,
                          CorePortal corePortal)
    {
        super(coreChat,
              "support",
              "<Message>",
              "Request help from a staff member.",
              Set.of("a", "admin", "helpop", "sc", "staffchat"),
              CoreChat.PERM.COMMAND_SUPPORT);
        _corePermission = corePermission;
        _coreDatabase = coreDatabase;
        _corePortal = corePortal;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        if (!(sender instanceof Player player))
        {
            sender.sendMessage(F.fMain(this, F.fError("Only players can execute this command.")));
            return;
        }

        if (!_miniPlugin._receivedTipSet.contains(player))
        {
            _miniPlugin._receivedTipSet.add(player);
            player.sendMessage(F.fMain(this,
                                       "You should receive a reply shortly if a staff member is available. You can also report rule-breakers with ",
                                       F.fItem("/report"),
                                       "."));
        }

        _miniPlugin._hexusPlugin.runAsync(() ->
                                          {
                                              try
                                              {
                                                  _coreDatabase._database._jedis.publish(ChatSupportMessage.CHANNEL_NAME,
                                                                                         new ChatSupportMessage(player.getUniqueId(),
                                                                                                                player.getName(),
                                                                                                                _corePermission._permissionProfiles.get(
                                                                                                                                       player)
                                                                                                                                                   ._groups(),
                                                                                                                _corePortal._serverName,
                                                                                                                String.join(
                                                                                                                        " ",
                                                                                                                        args)).toString());
                                              }
                                              catch (JedisException ex)
                                              {
                                                  sender.sendMessage(F.fMain(this,
                                                                             F.fError(
                                                                                     "An error occurred while sending your support message. Maybe try again later?")));

                                                  _miniPlugin.logWarning("JedisException while player '" +
                                                                         sender.getName() +
                                                                         "' sending support message: " +
                                                                         ex.getMessage());
                                              }
                                          });
    }

}
