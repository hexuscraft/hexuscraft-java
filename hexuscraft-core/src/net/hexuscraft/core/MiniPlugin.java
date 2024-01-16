package net.hexuscraft.core;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Map;

public abstract class MiniPlugin<T extends HexusPlugin> implements Listener {

    public final T _plugin;
    public final String _name;

    protected MiniPlugin(T plugin, String name) {
        _plugin = plugin;
        _name = name;

        log("Instantiated.");
    }

    public void log(String message) {
        _plugin.getLogger().info("[" + _name + "] " + message);
    }

    public final void load(Map<Class<? extends MiniPlugin<HexusPlugin>>, MiniPlugin<HexusPlugin>> miniPluginClassMap) {
        log("Initializing...");
        long start = System.currentTimeMillis();

        onLoad(miniPluginClassMap);

        log("Initialized in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void enable() {
        log("Enabling...");
        long start = System.currentTimeMillis();

        _plugin.getServer().getPluginManager().registerEvents(this, _plugin);
        onEnable();

        log("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void disable() {
        log("Disabling...");
        long start = System.currentTimeMillis();

        HandlerList.unregisterAll(this);
        onDisable();

        log("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void onLoad(Map<Class<? extends MiniPlugin<HexusPlugin>>, MiniPlugin<HexusPlugin>> dependencies) {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

}
