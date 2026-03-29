package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.permission.CorePermission;

import java.util.Set;

public class CommandRank extends BaseMultiCommand<CorePermission>
{

    public CommandRank(CorePermission corePermission, CoreDatabase coreDatabase)
    {
        super(corePermission,
              "rank",
              "Manage permission groups of players.",
              Set.of("ranks", "perm", "perms", "permission", "permissions"),
              CorePermission.PERM.COMMAND_RANK,
              Set.of(new CommandRankAdd(corePermission, coreDatabase),
                     new CommandRankClear(corePermission, coreDatabase),
                     new CommandRankInfo(corePermission, coreDatabase),
                     new CommandRankList(corePermission),
                     new CommandRankRemove(corePermission, coreDatabase)));
    }

}
