package net.hexuscraft.hub.news;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.NewsData;
import net.hexuscraft.common.database.messages.NewsDeletedMessage;
import net.hexuscraft.common.database.messages.NewsUpdatedMessage;
import net.hexuscraft.common.database.queries.NewsQueries;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.bossbar.BossBar;
import net.hexuscraft.core.bossbar.MiniPluginBossBar;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.hub.Hub;
import net.hexuscraft.hub.news.command.CommandNews;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class MiniPluginNews extends MiniPlugin<Hub> {

    public enum PERM implements IPermission {
        COMMAND_NEWS,
        COMMAND_NEWS_ADD,
        COMMAND_NEWS_REMOVE,
        COMMAND_NEWS_LIST,
        COMMAND_NEWS_MODIFY,
        COMMAND_NEWS_SET,
        COMMAND_NEWS_SET_ACTIVE,
        COMMAND_NEWS_SET_WEIGHT,
        COMMAND_NEWS_SET_MESSAGE,
    }

    private MiniPluginBossBar _miniPluginBossBar;
    private MiniPluginCommand _miniPluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private Map<Player, BossBar> _bossBars;
    private List<NewsData> _newsDatas;
    private List<NewsData> _activeNews;
    private AtomicInteger _activeNewsIndex;

    public MiniPluginNews(final Hub hub) {
        super(hub,
                "News");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginBossBar = (MiniPluginBossBar) dependencies.get(MiniPluginBossBar.class);
        _miniPluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _bossBars = new HashMap<>();
        _newsDatas = new ArrayList<>();
        _activeNews = new ArrayList<>();
        _activeNewsIndex = new AtomicInteger();
    }

    @Override
    public void onEnable() {
        _miniPluginDatabase._database.registerConsumer(NewsUpdatedMessage.CHANNEL_NAME,
                (_, _, rawMessage) -> {
                    final NewsUpdatedMessage parsedMessage = NewsUpdatedMessage.parse(rawMessage);

                    _hexusPlugin.runAsync(() -> {
                        final NewsData newNewsData = NewsQueries.getNews(_miniPluginDatabase._database._jedis,
                                parsedMessage._id);
                        if (newNewsData == null) return; // silences the linter

                        _newsDatas.removeIf(newsData -> newsData._id.equals(newNewsData._id));
                        _newsDatas.add(newNewsData);
                        _newsDatas.sort(Comparator.comparing(newsData -> newsData._weight));
                    });
                });

        _miniPluginDatabase._database.registerConsumer(NewsDeletedMessage.CHANNEL_NAME,
                (_, _, rawMessage) -> {
                    final NewsDeletedMessage parsedMessage = NewsDeletedMessage.parse(rawMessage);
                    _newsDatas.removeIf(newsData -> newsData._id.equals(parsedMessage._id));
                });

        _hexusPlugin.runAsync(() -> {
            final NewsData[] news = NewsQueries.getNews(_miniPluginDatabase._database._jedis);
            _newsDatas.clear();
            _newsDatas.addAll(Arrays.asList(news));
            _newsDatas.sort(Comparator.comparing(newsData -> newsData._weight));
        });

        _hexusPlugin.runAsyncTimer(() -> {
                    _activeNews.clear();
                    _activeNews.addAll(_newsDatas.stream()
                            .filter(newsData -> newsData._active)
                            .toList());

                    if (_activeNews.isEmpty()) {
                        _bossBars.values()
                                .forEach(bossBar -> bossBar.message()
                                        .set(C.cGold + C.fBold + "HEXUSCRAFT"));
                        return;
                    }

                    if (_activeNewsIndex.incrementAndGet() >= _activeNews.size()) _activeNewsIndex.set(0);

                    _bossBars.values()
                            .forEach(bossBar -> bossBar.message()
                                    .set(_activeNews.get(_activeNewsIndex.get())._message));
                },
                0,
                100); // 5 seconds

        _miniPluginCommand.register(new CommandNews(this,
                _miniPluginDatabase));
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        _bossBars.put(player,
                _miniPluginBossBar.registerBossBar(new BossBar(player,
                        0,
                        "<news loading>")));
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (!_bossBars.containsKey(player)) return;
        _miniPluginBossBar.unregisterBossBar(_bossBars.get(player));
        _bossBars.remove(player);
    }

}
