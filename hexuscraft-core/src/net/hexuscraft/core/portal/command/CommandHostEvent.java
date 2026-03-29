package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.database.data.ServerData;
import net.hexuscraft.common.database.data.ServerGroupData;
import net.hexuscraft.common.enums.GameType;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Set;

public final class CommandHostEvent extends BaseCommand<CorePortal>
{

    private final CoreDatabase _coreDatabase;

    public CommandHostEvent(final CorePortal corePortal, final CoreDatabase coreDatabase)
    {
        super(corePortal,
              "hostevent",
              "",
              "Start the event server or teleport to the existing event server.",
              Set.of("hes", "mes", "hosthes", "hostmes"),
              CorePortal.PERM.COMMAND_HOSTEVENT);

        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {
        if (args.length > 0)
        {
            sender.sendMessage(help(alias));
            return;
        }

        final String serverGroupName = "Event";

        final ServerData[] existingServers = _miniPlugin.getServers(serverGroupName);
        if (existingServers.length > 0)
        {
            if (!(sender instanceof final Player player))
            {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport to their private server.")));
                return;
            }

            _miniPlugin.teleport(player, existingServers[0]._name);
            return;
        }

        if (_miniPlugin.getServerGroup(serverGroupName) != null)
        {
            sender.sendMessage(F.fMain(this,
                                       "Your server is currently being created. You will be teleported shortly."));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() ->
                                          {
                                              try
                                              {
                                                  new ServerGroupData(serverGroupName,
                                                                      PermissionGroup._PLAYER,
                                                                      CorePortal.EVENT_SERVER_PORT,
                                                                      CorePortal.EVENT_SERVER_PORT,
                                                                      1,
                                                                      0,
                                                                      "Arcade.jar",
                                                                      "Arcade.zip",
                                                                      2048,
                                                                      100,
                                                                      false,
                                                                      10000,
                                                                      new GameType[]{GameType.SURVIVAL_GAMES},
                                                                      sender instanceof final Player player ?
                                                                      player.getUniqueId() :
                                                                      UtilUniqueId.EMPTY_UUID).update(_coreDatabase._database._jedis);
                                              }
                                              catch (final JedisException ex)
                                              {
                                                  sender.sendMessage(F.fMain(this,
                                                                             F.fError(
                                                                                     "There was an error creating your server. Please try again later or contact an administrator if this issue persists.")));
                                                  return;
                                              }
                                              sender.sendMessage(F.fMain(this,
                                                                         F.fSuccess(
                                                                                 "Successfully created your server. You will be automatically teleported once your server has started. This may take up to 30 seconds.")));
                                          });
    }
}
