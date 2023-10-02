package net.hexuscraft.core.permission.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.scoreboard.PluginScoreboard;

import java.util.Set;

public class CommandRank extends BaseMultiCommand {

    public CommandRank(PluginPermission pluginPermission, PluginDatabase pluginDatabase, PluginScoreboard pluginScoreboard) {
        super(pluginPermission, "rank", "Manage permission groups of players.", Set.of("ranks", "perm", "perms", "permission", "permissions"), PluginPermission.PERM.COMMAND_RANK, Set.of(
                new CommandRankAdd(pluginPermission, pluginDatabase, pluginScoreboard),
                new CommandRankClear(pluginPermission, pluginDatabase, pluginScoreboard),
                new CommandRankInfo(pluginPermission, pluginDatabase),
                new CommandRankList(pluginPermission),
                new CommandRankRemove(pluginPermission, pluginDatabase, pluginScoreboard),
                new CommandRankSet(pluginPermission, pluginDatabase, pluginScoreboard)
        ));
    }

}
