package net.hexuscraft.hub.news.command;

import net.hexuscraft.core.command.BaseMultiCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.hub.news.HubNews;

import java.util.Set;

public final class CommandNews extends BaseMultiCommand<HubNews>
{

    public CommandNews(final HubNews hubNews, final CoreDatabase coreDatabase)
    {
        super(hubNews,
              "news",
              "Manage the hub news.",
              Set.of(),
              HubNews.PERM.COMMAND_NEWS,
              Set.of(new CommandNewsAdd(hubNews, coreDatabase)));
    }

}
