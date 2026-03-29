package net.hexuscraft.core.permission.command;

import net.hexuscraft.common.database.queries.PermissionQueries;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandRankClear extends BaseCommand<CorePermission>
{

    private final CoreDatabase _coreDatabase;

    public CommandRankClear(CorePermission permission, CoreDatabase database)
    {
        super(permission,
              "clear",
              "<Player>",
              "Clears a player's additional groups.",
              Set.of(),
              CorePermission.PERM.COMMAND_RANK_CLEAR);
        _coreDatabase = database;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length != 1)
        {
            sender.sendMessage(help(alias));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() ->
                                          {
                                              OfflinePlayer offlinePlayer = PlayerSearch.offlinePlayerSearch(args[0]);
                                              if (offlinePlayer == null)
                                              {
                                                  sender.sendMessage(F.fMatches(new String[]{}, args[0]));
                                                  return;
                                              }

                                              sender.sendMessage(F.fMain(this,
                                                                         "Clearing sub-groups of ",
                                                                         F.fItem(offlinePlayer.getName()),
                                                                         "..."));

                                              _coreDatabase._database._jedis.del(PermissionQueries.GROUPS(offlinePlayer.getUniqueId()));
                                              sender.sendMessage(F.fMain(this,
                                                                         F.fSuccess("Cleared sub-groups of ",
                                                                                    F.fItem(offlinePlayer.getName()),
                                                                                    ".")));
                                          });
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        List<String> names = new ArrayList<>();
        if (args.length == 1)
        {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._hexusPlugin.getServer()
                                                                                     .getOnlinePlayers()
                                                                                     .stream();
            if (sender instanceof Player player)
            {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
