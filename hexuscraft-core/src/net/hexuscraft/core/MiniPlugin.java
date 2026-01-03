package net.hexuscraft.core;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public abstract class MiniPlugin<T extends HexusPlugin> implements Listener, IMiniPlugin {

    public final T _hexusPlugin;
    public final String _prefix;

    protected MiniPlugin(final T plugin, final String prefix) {
        final long start = System.currentTimeMillis();
        _hexusPlugin = plugin;
        _prefix = prefix;
        logInfo("Instantiated in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void load(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> miniPluginClassMap) {
        final long start = System.currentTimeMillis();
        logInfo("Loading...");

        onLoad(miniPluginClassMap);

        logInfo("Loaded in " + (System.currentTimeMillis() - start) + "ms.");
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

    @Override
    public String toString() {
        return _prefix;
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

    public void logSevere(final Exception ex) {
        logSevere("[" + ex.getClass().getName() + "] " + String.join("\n", Stream.concat(Stream.of(ex.getMessage()),
                Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString)).toArray(String[]::new)));
    }

}
