package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.MiniPluginAntiCheat;
import net.hexuscraft.core.authentication.MiniPluginAuthentication;
import net.hexuscraft.core.buildversion.MiniPluginBuildVersion;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.combat.MiniPluginCombat;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.cooldown.MiniPluginCooldown;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.disguise.MiniPluginDisguise;
import net.hexuscraft.core.gamemode.MiniPluginGameMode;
import net.hexuscraft.core.item.MiniPluginItem;
import net.hexuscraft.core.npc.MiniPluginNpc;
import net.hexuscraft.core.party.MiniPluginParty;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.core.punish.MiniPluginPunish;
import net.hexuscraft.core.report.MiniPluginReport;
import net.hexuscraft.core.scoreboard.MiniPluginScoreboard;
import net.hexuscraft.core.teleport.MiniPluginTeleport;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class HexusPlugin extends JavaPlugin implements IHexusPlugin, Listener {

    public final Server _server;
    public final PluginManager _pluginManager;
    public final BukkitScheduler _scheduler;
    public final Logger _logger;
    private final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>>
            _miniPluginClassMap;

    public HexusPlugin() {
        _miniPluginClassMap = new HashMap<>();
        _server = getServer();
        _pluginManager = _server.getPluginManager();
        _scheduler = _server.getScheduler();
        _logger = getLogger();
    }

    @Override
    public final void onLoad() {
        final AtomicLong start = new AtomicLong(System.currentTimeMillis());
        logInfo("Instantiating core plugins...");

        require(new MiniPluginAntiCheat(this));
        require(new MiniPluginAuthentication(this));
        require(new MiniPluginBuildVersion(this));
        require(new MiniPluginChat(this));
        require(new MiniPluginCombat(this));
        require(new MiniPluginCommand(this));
        require(new MiniPluginCooldown(this));
        require(new MiniPluginDatabase(this));
        require(new MiniPluginDisguise(this));
        require(new MiniPluginGameMode(this));
        require(new MiniPluginItem(this));
        require(new MiniPluginNpc(this));
        require(new MiniPluginParty(this));
        require(new MiniPluginPermission(this));
        require(new MiniPluginPortal(this));
        require(new MiniPluginPunish(this));
        require(new MiniPluginReport(this));
        require(new MiniPluginScoreboard(this));
        require(new MiniPluginTeleport(this));

        logInfo("Instantiated core plugins in " + (System.currentTimeMillis() - start.get()) + "ms.");

        start.set(System.currentTimeMillis());
        logInfo("Instantiating local plugins...");

        load();

        logInfo("Instantiated local plugins instantiated in " + (System.currentTimeMillis() - start.get()) + "ms.");

        start.set(System.currentTimeMillis());
        logInfo("Loading...");

        _miniPluginClassMap.values().forEach(miniPlugin -> miniPlugin.load(_miniPluginClassMap));

        logInfo("Loaded in " + (System.currentTimeMillis() - start.get()) + "ms.");
    }

    @Override
    public final void onEnable() {
        long start = System.currentTimeMillis();
        logInfo("Enabling...");

        _pluginManager.registerEvents(this, this);
        enable();
        _miniPluginClassMap.values().forEach(MiniPlugin::enable);

        logInfo("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public final void onDisable() {
        long start = System.currentTimeMillis();
        logInfo("Disabling...");

        disable();
        _miniPluginClassMap.values().forEach(MiniPlugin::disable);
        _miniPluginClassMap.clear();

        logInfo("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void require(final MiniPlugin<? extends HexusPlugin> miniPlugin) {
        //noinspection unchecked
        _miniPluginClassMap.put((Class<? extends MiniPlugin<? extends HexusPlugin>>) miniPlugin.getClass(), miniPlugin);
    }

    public void logInfo(final String message) {
        _logger.log(Level.INFO, message);
    }

    public void logWarning(final String message) {
        _logger.log(Level.WARNING, message);
    }

    public void logSevere(final String message) {
        _logger.log(Level.SEVERE, message);
    }

    public void logSevere(final Exception ex) {
        logSevere("[" + ex.getClass().getName() + "] " + String.join("\n", Stream.concat(Stream.of(ex.getMessage()),
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString)).toArray(String[]::new)));
    }

    public File getFile() {
        return super.getFile();
    }

    @Override
    public String toString() {
        return getName();
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask runSync(final Runnable runnable) {
        return _scheduler.runTask(this, runnable);
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask runSyncLater(final Runnable runnable, final long delayTicks) {
        return _scheduler.runTaskLater(this, runnable, delayTicks);
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask runSyncTimer(final Runnable runnable, final long initialDelayTicks, final long repeatEveryTicks) {
        return _scheduler.runTaskTimer(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

    public BukkitTask runAsync(final Runnable runnable) {
        return _scheduler.runTaskAsynchronously(this, runnable);
    }

    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask runAsyncLater(final Runnable runnable, final long delayTicks) {
        return _scheduler.runTaskLaterAsynchronously(this, runnable, delayTicks);
    }

    public BukkitTask runAsyncTimer(final Runnable runnable, final long initialDelayTicks,
                                    final long repeatEveryTicks) {
        return _scheduler.runTaskTimerAsynchronously(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

}
