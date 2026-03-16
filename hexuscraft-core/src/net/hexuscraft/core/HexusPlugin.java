package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.MiniPluginAntiCheat;
import net.hexuscraft.core.authentication.MiniPluginAuthentication;
import net.hexuscraft.core.bossbar.MiniPluginBossBar;
import net.hexuscraft.core.buildversion.MiniPluginBuildVersion;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.combat.MiniPluginCombat;
import net.hexuscraft.core.command.MiniPluginCommand;
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

    public final PluginManager _pluginManager;
    public final BukkitScheduler _scheduler;
    public final Logger _logger;
    private final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> _miniPlugins;

    public HexusPlugin() {
        final long start = System.currentTimeMillis();
        final Server server = getServer();
        _pluginManager = server.getPluginManager();
        _scheduler = server.getScheduler();
        _logger = getLogger();

        _miniPlugins = new HashMap<>();
        requireCorePlugins();
        logInfo("Instantiated in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public HexusPlugin(final boolean shouldRequireCorePlugins) {
        final Server server = getServer();
        _pluginManager = server.getPluginManager();
        _scheduler = server.getScheduler();
        _logger = getLogger();

        _miniPlugins = new HashMap<>();

        if (shouldRequireCorePlugins) requireCorePlugins();
    }

    public final void requireCorePlugins() {
        require(new MiniPluginAntiCheat(this));
        require(new MiniPluginAuthentication(this));
        require(new MiniPluginBossBar(this));
        require(new MiniPluginBuildVersion(this));
        require(new MiniPluginChat(this));
        require(new MiniPluginCombat(this));
        require(new MiniPluginCommand(this));
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
    }

    @Override
    public final void onLoad() {
        final AtomicLong start = new AtomicLong(System.currentTimeMillis());
        logInfo("Loading...");

        load();
        _miniPlugins.values()
                .forEach(miniPlugin -> miniPlugin.load(_miniPlugins));

        logInfo("Loaded in " + (System.currentTimeMillis() - start.get()) + "ms.");
    }

    @Override
    public final void onEnable() {
        long start = System.currentTimeMillis();
        logInfo("Enabling...");

        _pluginManager.registerEvents(this, this);
        enable();
        _miniPlugins.values()
                .forEach(MiniPlugin::enable);

        logInfo("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public final void onDisable() {
        long start = System.currentTimeMillis();
        logInfo("Disabling...");

        disable();
        _miniPlugins.values()
                .forEach(MiniPlugin::disable);
        _miniPlugins.clear();

        logInfo("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void require(final MiniPlugin<? extends HexusPlugin> miniPlugin) {
        //noinspection unchecked
        _miniPlugins.put((Class<? extends MiniPlugin<? extends HexusPlugin>>) miniPlugin.getClass(), miniPlugin);
    }

    public final void logInfo(final String message) {
        _logger.log(Level.INFO, message);
    }

    public final void logInfo(final Exception ex) {
        logInfo("[" + ex.getClass()
                .getName() + "] " + String.join("\n", Stream.concat(Stream.of(ex.getMessage()),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString))
                .toArray(String[]::new)));
    }

    public final void logWarning(final String message) {
        _logger.log(Level.WARNING, message);
    }

    public final void logWarning(final Exception ex) {
        logWarning("[" + ex.getClass()
                .getName() + "] " + String.join("\n", Stream.concat(Stream.of(ex.getMessage()),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString))
                .toArray(String[]::new)));
    }

    public final void logSevere(final String message) {
        _logger.log(Level.SEVERE, message);
    }

    public final void logSevere(final Exception ex) {
        logSevere("[" + ex.getClass()
                .getName() + "] " + String.join("\n", Stream.concat(Stream.of(ex.getMessage()),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString))
                .toArray(String[]::new)));
    }

    public final File getFile() {
        return super.getFile();
    }

    @Override
    public final String toString() {
        return getName();
    }

    public final BukkitTask runSync(final Runnable runnable) {
        return _scheduler.runTask(this, runnable);
    }

    public final BukkitTask runSyncLater(final Runnable runnable, final long delayTicks) {
        return _scheduler.runTaskLater(this, runnable, delayTicks);
    }

    public final BukkitTask runSyncTimer(final Runnable runnable, final long initialDelayTicks, final long repeatEveryTicks) {
        return _scheduler.runTaskTimer(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

    public final BukkitTask runAsync(final Runnable runnable) {
        return _scheduler.runTaskAsynchronously(this, runnable);
    }

    public final BukkitTask runAsyncLater(final Runnable runnable, final long delayTicks) {
        return _scheduler.runTaskLaterAsynchronously(this, runnable, delayTicks);
    }

    public final BukkitTask runAsyncTimer(final Runnable runnable, final long initialDelayTicks, final long repeatEveryTicks) {
        return _scheduler.runTaskTimerAsynchronously(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

}
