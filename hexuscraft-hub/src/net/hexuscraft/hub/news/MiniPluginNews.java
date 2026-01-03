package net.hexuscraft.hub.news;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.bossbar.BossBar;
import net.hexuscraft.core.bossbar.MiniPluginBossBar;
import net.hexuscraft.hub.Hub;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public final class MiniPluginNews extends MiniPlugin<Hub> {

    private MiniPluginBossBar _miniPluginBossBar;

    public MiniPluginNews(final Hub hub) {
        super(hub, "News");
    }

    @Override
    public void onLoad(final
                       Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _miniPluginBossBar = (MiniPluginBossBar) dependencies.get(MiniPluginBossBar.class);
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final BossBar bossBar = _miniPluginBossBar.registerBossBar(event.getPlayer());
        bossBar._message.set("§6§lTest of the hub news system");
        bossBar._progress.set(0.5f);
    }

}
