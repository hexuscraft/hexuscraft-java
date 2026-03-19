package net.hexuscraft.hub.news.command;

import net.hexuscraft.common.database.data.NewsData;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.hub.news.HubNews;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class CommandNewsAdd extends BaseCommand<HubNews> {

    private final Set<String> POSITIVES = Set.of("true",
            "yes",
            "1");
    private final Set<String> NEGATIVES = Set.of("false",
            "no",
            "0");

    private final CoreDatabase _coreDatabase;

    public CommandNewsAdd(final HubNews hubNews, final CoreDatabase coreDatabase) {
        super(hubNews,
                "add",
                "<Active TRUE/FALSE> <Weight #> <Message>",
                "Add a news line.",
                Set.of("a"),
                HubNews.PERM.COMMAND_NEWS_ADD);
        _coreDatabase = coreDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 3) {
            sender.sendMessage(help(alias));
            return;
        }

        final AtomicBoolean active = new AtomicBoolean(false);
        if (POSITIVES.contains(args[0].toLowerCase())) active.set(true);
        else if (!NEGATIVES.contains(args[0].toLowerCase())) {
            sender.sendMessage(help(alias));
            return;
        }

        final AtomicInteger weight = new AtomicInteger(0);
        try {
            weight.set(Integer.parseInt(args[1]));
        } catch (final NumberFormatException ex) {
            sender.sendMessage(F.fMain(this,
                    F.fError("There was an error while parsing weight ",
                            F.fItem(args[1]),
                            ". Defaulting to ",
                            F.fItem(String.valueOf(weight.get())),
                            ".")));
        }

        final String message = String.join(" ",
                Arrays.copyOfRange(args,
                        2,
                        args.length));

        final NewsData newsData = new NewsData(UUID.randomUUID(),
                Map.ofEntries(Map.entry("active",
                                Boolean.toString(active.get())),
                        Map.entry("weight",
                                Integer.toString(weight.get())),
                        Map.entry("message",
                                message)));

        try {
            newsData.publish(_coreDatabase._database._jedis);
            sender.sendMessage(F.fMain(this,
                    F.fSuccess("Successfully added news line.")));
        } catch (final JedisException ex) {
            sender.sendMessage(F.fMain(this,
                    F.fError(
                            "There was an error while publishing news. Please try again later or contact an administrator if this issue persists.")));
        }
    }
}
