package net.hexuscraft.core;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;
import java.util.OptionalDouble;

public abstract class MiniPlugin implements Listener {

    public final JavaPlugin _javaPlugin;
    public final String _name;

    protected MiniPlugin(JavaPlugin javaPlugin, String name) {
        _javaPlugin = javaPlugin;
        _name = name;

        log("Instantiated.");
    }

    public void log(String message) {
        _javaPlugin.getLogger().info("[" + _name + "] " + message);
    }

    public final void load(Map<Class<? extends MiniPlugin>, MiniPlugin> miniPluginClassMap) {
        log("Initializing...");
        long start = System.currentTimeMillis();

        onLoad(miniPluginClassMap);

        log("Initialized in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void enable() {
        log("Enabling...");
        long start = System.currentTimeMillis();

        _javaPlugin.getServer().getPluginManager().registerEvents(this, _javaPlugin);
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

    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

}
