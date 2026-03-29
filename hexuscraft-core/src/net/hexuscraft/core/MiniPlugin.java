package net.hexuscraft.core;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public abstract class MiniPlugin<T extends HexusPlugin> implements Listener, IMiniPlugin
{

    public T _hexusPlugin;
    public String _prefix;

    public MiniPlugin(T plugin, String prefix)
    {
        long start = System.currentTimeMillis();
        _hexusPlugin = plugin;
        _prefix = prefix;
        logInfo("Instantiated in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void load(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> miniPluginClassMap)
    {
        long start = System.currentTimeMillis();
        logInfo("Loading...");

        onLoad(miniPluginClassMap);

        logInfo("Loaded in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void enable()
    {
        long start = System.currentTimeMillis();
        logInfo("Enabling...");

        _hexusPlugin.getServer().getPluginManager().registerEvents(this, _hexusPlugin);
        onEnable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L)
        {
            logWarning("Took " + (System.currentTimeMillis() - start) + "ms to enable. (>2s)");
        }
        logInfo("String.valueOf " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void disable()
    {
        long start = System.currentTimeMillis();
        logInfo("Disabling...");

        HandlerList.unregisterAll(this);
        onDisable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L)
        {
            logWarning("Took " + (System.currentTimeMillis() - start) + "ms to disable. (>2s)");
        }
        logInfo("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public String toString()
    {
        return _prefix;
    }

    public void logInfo(String message)
    {
        _hexusPlugin.logInfo("[" + _prefix + "] " + message);
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
        _hexusPlugin.logWarning("[" + _prefix + "] " + message);
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
        _hexusPlugin.logSevere("[" + _prefix + "] " + message);
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

}
