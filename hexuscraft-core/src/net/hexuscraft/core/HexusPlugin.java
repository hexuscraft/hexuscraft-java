package net.hexuscraft.core;

import net.hexuscraft.core.actionbar.CoreActionBar;
import net.hexuscraft.core.anticheat.CoreAntiCheat;
import net.hexuscraft.core.authentication.CoreTwoFactorAuthentication;
import net.hexuscraft.core.bossbar.CoreBossBar;
import net.hexuscraft.core.buildversion.CoreBuildVersion;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.combat.CoreCombat;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.disguise.CoreDisguise;
import net.hexuscraft.core.featureflags.CoreFeatureFlags;
import net.hexuscraft.core.gamemode.CoreGameMode;
import net.hexuscraft.core.gui.CoreGui;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class HexusPlugin extends JavaPlugin implements IHexusPlugin, Listener
{

    final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> _miniPlugins;
    final boolean _isDebug;
    public Server _server;
    public BukkitScheduler _scheduler;
    public Logger _logger;

    public HexusPlugin()
    {
        this(true);
    }

    public HexusPlugin(boolean shouldRequireCorePlugins)
    {
        long start = System.currentTimeMillis();
        _server = getServer();
        _scheduler = _server.getScheduler();
        _logger = getLogger();

        _miniPlugins = new HashMap<>();
        if (shouldRequireCorePlugins)
        {
            requireCorePlugins();
        }

        _isDebug = getIsDebug();

        logInfo("Instantiated in " + (System.currentTimeMillis() - start) + "ms.");
    }

    boolean getIsDebug()
    {
        File debugFile = new File("_debug.dat");
        if (debugFile.exists())
        {
            try
            {
                return Boolean.parseBoolean(readFile(debugFile)[0]);
            }
            catch (FileNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        return false;
    }

    public void requireCorePlugins()
    {
        require(new CoreActionBar(this));
        require(new CoreAntiCheat(this));
        require(new CoreTwoFactorAuthentication(this));
        require(new CoreBossBar(this));
        require(new CoreBuildVersion(this));
        require(new CoreChat(this));
        require(new CoreCombat(this));
        require(new CoreCommand(this));
        require(new CoreDatabase(this));
        require(new CoreDisguise(this));
        require(new CoreFeatureFlags(this));
        require(new CoreGameMode(this));
        require(new CoreGui(this));
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
    public void onLoad()
    {
        AtomicLong start = new AtomicLong(System.currentTimeMillis());
        logInfo("Loading...");

        load();
        _miniPlugins.values().forEach(miniPlugin -> miniPlugin.load(_miniPlugins));

        logInfo("Loaded in " + (System.currentTimeMillis() - start.get()) + "ms.");
    }

    @Override
    public void onEnable()
    {
        long start = System.currentTimeMillis();
        logInfo("Enabling...");

        _server.getPluginManager().registerEvents(this, this);
        enable();
        _miniPlugins.values().forEach(MiniPlugin::enable);

        logInfo("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onDisable()
    {
        long start = System.currentTimeMillis();
        logInfo("Disabling...");

        disable();
        _miniPlugins.values().forEach(MiniPlugin::disable);
        _miniPlugins.clear();

        logInfo("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public String[] readFile(File file) throws FileNotFoundException
    {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file))
        {
            while (scanner.hasNextLine())
                lines.add(scanner.nextLine());
        }
        return lines.toArray(String[]::new);
    }

    public void require(MiniPlugin<? extends HexusPlugin> miniPlugin)
    {
        //noinspection unchecked
        _miniPlugins.put((Class<? extends MiniPlugin<? extends HexusPlugin>>) miniPlugin.getClass(), miniPlugin);
    }

    public void logInfo(String message)
    {
        _logger.log(Level.INFO, message);
    }

    public void logInfo(Throwable ex)
    {
        logInfo("[" +
                ex.getClass().getName() +
                "] " +
                String.join("\n",
                        Stream.concat(Stream.of(ex.getMessage()),
                                        Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString))
                                .toArray(String[]::new)));
    }

    public void logWarning(String message)
    {
        _logger.log(Level.WARNING, "[WARNING] " + message);
    }

    public void logWarning(Throwable ex)
    {
        logWarning("[" +
                ex.getClass().getName() +
                "] " +
                String.join("\n",
                        Stream.concat(Stream.of(ex.getMessage()),
                                        Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString))
                                .toArray(String[]::new)));
    }

    public void logSevere(String message)
    {
        _logger.log(Level.SEVERE, "[SEVERE] " + message);
    }

    public void logSevere(Throwable ex)
    {
        logSevere("[" +
                ex.getClass().getName() +
                "] " +
                String.join("\n",
                        Stream.concat(Stream.of(ex.getMessage()),
                                        Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString))
                                .toArray(String[]::new)));
    }

    public File getFile()
    {
        return super.getFile();
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public BukkitTask runSync(Runnable runnable)
    {
        return _scheduler.runTask(this, runnable);
    }

    public BukkitTask runSyncLater(Runnable runnable, long delayTicks)
    {
        return _scheduler.runTaskLater(this, runnable, delayTicks);
    }

    public BukkitTask runSyncTimer(Runnable runnable, long initialDelayTicks, long repeatEveryTicks)
    {
        return _scheduler.runTaskTimer(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

    public BukkitTask runAsync(Runnable runnable)
    {
        return _scheduler.runTaskAsynchronously(this, runnable);
    }

    public BukkitTask runAsyncLater(Runnable runnable, long delayTicks)
    {
        return _scheduler.runTaskLaterAsynchronously(this, runnable, delayTicks);
    }

    public BukkitTask runAsyncTimer(Runnable runnable, long initialDelayTicks, long repeatEveryTicks)
    {
        return _scheduler.runTaskTimerAsynchronously(this, runnable, initialDelayTicks, repeatEveryTicks);
    }

}
