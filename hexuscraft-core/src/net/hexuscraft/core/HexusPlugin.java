package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.CoreAntiCheat;
import net.hexuscraft.core.authentication.CoreAuthentication;
import net.hexuscraft.core.bossbar.CoreBossBar;
import net.hexuscraft.core.buildversion.CoreBuildVersion;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.combat.CoreCombat;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.disguise.CoreDisguise;
import net.hexuscraft.core.featureflags.CoreFeatureFlags;
import net.hexuscraft.core.gamemode.CoreGameMode;
import net.hexuscraft.core.item.CoreItem;
import net.hexuscraft.core.npc.CoreNpc;
import net.hexuscraft.core.party.CoreParty;
import net.hexuscraft.core.permission.CorePermission;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.punish.CorePunish;
import net.hexuscraft.core.report.CoreReport;
import net.hexuscraft.core.scoreboard.CoreScoreboard;
import net.hexuscraft.core.teleport.CoreTeleport;
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
        require(new CoreAntiCheat(this));
        require(new CoreAuthentication(this));
        require(new CoreBossBar(this));
        require(new CoreBuildVersion(this));
        require(new CoreChat(this));
        require(new CoreCombat(this));
        require(new CoreCommand(this));
        require(new CoreDatabase(this));
        require(new CoreDisguise(this));
        require(new CoreFeatureFlags(this));
        require(new CoreGameMode(this));
        require(new CoreItem(this));
        require(new CoreNpc(this));
        require(new CoreParty(this));
        require(new CorePermission(this));
        require(new CorePortal(this));
        require(new CorePunish(this));
        require(new CoreReport(this));
        require(new CoreScoreboard(this));
        require(new CoreTeleport(this));
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
