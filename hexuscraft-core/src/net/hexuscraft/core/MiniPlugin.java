package net.hexuscraft.core;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Map;

public abstract class MiniPlugin<T extends HexusPlugin> implements Listener, IMiniPlugin {

    public final T _hexusPlugin;
    public final String _prefix;

    protected MiniPlugin(final T plugin, final String prefix) {
        _hexusPlugin = plugin;
        _prefix = prefix;

        logInfo("Instantiated.");
    }

    public final void load(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> miniPluginClassMap) {
        long start = System.currentTimeMillis();
        logInfo("Initializing...");

        onLoad(miniPluginClassMap);

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L) logWarning("Took " + (System.currentTimeMillis() - start) + "ms to enable. (>2s)");
        logInfo("Initialized in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void enable() {
        long start = System.currentTimeMillis();
        logInfo("Enabling...");

        _hexusPlugin.getServer().getPluginManager().registerEvents(this, _hexusPlugin);
        onEnable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L) logWarning("Took " + (System.currentTimeMillis() - start) + "ms to enable. (>2s)");
        logInfo("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void disable() {
        long start = System.currentTimeMillis();
        logInfo("Disabling...");

        HandlerList.unregisterAll(this);
        onDisable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L) logWarning("Took " + (System.currentTimeMillis() - start) + "ms to disable. (>2s)");
        logInfo("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void logInfo(final String message) {
        _hexusPlugin.logInfo("[" + _prefix + "] " + message);
    }

    public void logWarning(final String message) {
        _hexusPlugin.logWarning("[" + _prefix + "] " + message);
    }

    public void logSevere(final String message) {
        _hexusPlugin.logSevere("[" + _prefix + "] " + message);
    }

}
