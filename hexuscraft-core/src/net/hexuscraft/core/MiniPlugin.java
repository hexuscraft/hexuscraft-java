package net.hexuscraft.core;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Map;

public abstract class MiniPlugin<T extends HexusPlugin> implements Listener {

    public final T _hexusPlugin;
    public final String _name;

    protected MiniPlugin(final T plugin, final String name) {
        _hexusPlugin = plugin;
        _name = name;

        if (plugin._isDebug) log("Instantiated.");
    }

    public void log(final String message) {
        _hexusPlugin.getLogger().info("[" + _name + "] " + message);
    }

    public void warning(final String message) {
        _hexusPlugin.getLogger().warning("[" +  _name + "] " + message);
    }

    public final void load(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> miniPluginClassMap) {
        long start = System.currentTimeMillis();
        if (_hexusPlugin._isDebug) log("Initializing...");

        onLoad(miniPluginClassMap);

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L)
            log("WARNING: Took " + (System.currentTimeMillis() - start) + "ms to enable. (>2s)");
        if (_hexusPlugin._isDebug) log("Initialized in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void enable() {
        long start = System.currentTimeMillis();
        if (_hexusPlugin._isDebug) log("Enabling...");

        _hexusPlugin.getServer().getPluginManager().registerEvents(this, _hexusPlugin);
        onEnable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L)
            log("WARNING: Took " + (System.currentTimeMillis() - start) + "ms to enable. (>2s)");
        if (_hexusPlugin._isDebug) log("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void disable() {
        long start = System.currentTimeMillis();
        if (_hexusPlugin._isDebug) log("Disabling...");

        HandlerList.unregisterAll(this);
        onDisable();

        long finish = System.currentTimeMillis();
        if (finish - start > 2000L)
            log("WARNING: Took " + (System.currentTimeMillis() - start) + "ms to disable. (>2s)");
        if (_hexusPlugin._isDebug) log("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

}
