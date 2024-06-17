package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;

import java.util.Set;

public final class CommandRank extends BaseMultiCommand<MiniPluginPermission> {

    public CommandRank(final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPermission, "rank", "Manage permission groups of players.", Set.of("ranks", "perm", "perms", "permission", "permissions"), MiniPluginPermission.PERM.COMMAND_RANK, Set.of(
                new CommandRankAdd(miniPluginPermission, miniPluginDatabase),
                new CommandRankClear(miniPluginPermission, miniPluginDatabase),
                new CommandRankInfo(miniPluginPermission, miniPluginDatabase),
                new CommandRankList(miniPluginPermission),
                new CommandRankRemove(miniPluginPermission, miniPluginDatabase),
                new CommandRankSet(miniPluginPermission, miniPluginDatabase)
        ));
    }

}
