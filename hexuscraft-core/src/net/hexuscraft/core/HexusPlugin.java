package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.PluginCheat;
import net.hexuscraft.core.authentication.PluginAuth;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.combat.PluginCombat;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.disguise.PluginDisguise;
import net.hexuscraft.core.gamemode.PluginGameMode;
import net.hexuscraft.core.item.PluginItem;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.core.punish.PluginPunish;
import net.hexuscraft.core.report.PluginReport;
import net.hexuscraft.core.scoreboard.PluginScoreboard;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HexusPlugin extends JavaPlugin {

    Map<Class<? extends MiniPlugin>, MiniPlugin> _miniPluginClassMap;

    @Override
    public final void onLoad() {
        log("Loading...");
        long start = System.currentTimeMillis();

        _miniPluginClassMap = new HashMap<>();

        register(new PluginAuth(this));
        register(new PluginCheat(this));
        register(new PluginChat(this));
        register(new PluginCombat(this));
        register(new PluginCommand(this));
        register(new PluginDatabase(this));
        register(new PluginDisguise(this));
        register(new PluginGameMode(this));
        register(new PluginItem(this));
        register(new PluginPermission(this));
        register(new PluginPortal(this));
        register(new PluginPunish(this));
        register(new PluginReport(this));
        register(new PluginScoreboard(this));

        load();
        _miniPluginClassMap.values().forEach(miniPlugin -> miniPlugin.load(_miniPluginClassMap));

        log("Loaded in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void load() {
    }

    @Override
    public final void onEnable() {
        log("Enabling...");
        long start = System.currentTimeMillis();

        enable();
        _miniPluginClassMap.values().forEach(MiniPlugin::enable);

        log("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @SuppressWarnings("EmptyMethod")
    public void enable() {
    }

    @Override
    public final void onDisable() {
        log("Disabling...");
        long start = System.currentTimeMillis();

        disable();
        _miniPluginClassMap.values().forEach(MiniPlugin::disable);
        _miniPluginClassMap.clear();

        log("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @SuppressWarnings("EmptyMethod")
    public void disable() {
    }

    public final void register(MiniPlugin miniPlugin) {
        _miniPluginClassMap.put(miniPlugin.getClass(), miniPlugin);
    }

    public final void log(String message) {
        getLogger().info(message);
    }

    public final File getFile() {
        return super.getFile();
    }

}
