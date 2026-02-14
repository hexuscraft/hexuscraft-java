package net.hexuscraft.hub.news.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.hub.news.MiniPluginNews;

import java.util.Set;

public final class CommandNews extends BaseMultiCommand<MiniPluginNews> {

    public CommandNews(final MiniPluginNews miniPluginNews, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginNews,
                "news",
                "Manage the hub news.",
                Set.of(),
                MiniPluginNews.PERM.COMMAND_NEWS,
                Set.of(
                        new CommandNewsAdd(miniPluginNews,
                                miniPluginDatabase)
                ));
    }

}
