package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PluginPermission;

import java.util.Set;

public class CommandRank extends BaseMultiCommand {

    public CommandRank(final PluginPermission pluginPermission, final PluginDatabase pluginDatabase) {
        super(pluginPermission, "rank", "Manage permission groups of players.", Set.of("ranks", "perm", "perms", "permission", "permissions"), PluginPermission.PERM.COMMAND_RANK, Set.of(
                new CommandRankAdd(pluginPermission, pluginDatabase),
                new CommandRankClear(pluginPermission, pluginDatabase),
                new CommandRankInfo(pluginPermission, pluginDatabase),
                new CommandRankList(pluginPermission),
                new CommandRankRemove(pluginPermission, pluginDatabase),
                new CommandRankSet(pluginPermission, pluginDatabase)
        ));
    }

}
