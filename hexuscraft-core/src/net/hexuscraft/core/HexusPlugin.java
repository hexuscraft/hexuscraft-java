package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.PluginAntiCheat;
import net.hexuscraft.core.authentication.PluginAuthentication;
import net.hexuscraft.core.buildversion.PluginBuildVersion;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.combat.PluginCombat;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.cooldown.PluginCooldown;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.entity.PluginEntity;
import net.hexuscraft.core.gamemode.PluginGameMode;
import net.hexuscraft.core.item.PluginItem;
import net.hexuscraft.core.netstat.PluginNetStat;
import net.hexuscraft.core.party.PluginParty;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.core.punish.PluginPunish;
import net.hexuscraft.core.report.PluginReport;
import net.hexuscraft.core.scoreboard.PluginScoreboard;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class HexusPlugin extends JavaPlugin implements IHexusPlugin {

    private final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> _miniPluginClassMap;

    private final boolean _isDebug;

    public HexusPlugin() {
        _miniPluginClassMap = new HashMap<>();
        _isDebug = new File("_debug.dat").isFile();
    }

    @Override
    public final void onLoad() {
        long start = System.currentTimeMillis();

        require(new PluginAntiCheat(this));
        require(new PluginAuthentication(this));
        require(new PluginBuildVersion(this));
        require(new PluginChat(this));
        require(new PluginCombat(this));
        require(new PluginCommand(this));
        require(new PluginCooldown(this));
        require(new PluginDatabase(this));
        require(new PluginEntity(this));
        require(new PluginGameMode(this));
        require(new PluginItem(this));
        require(new PluginNetStat(this));
        require(new PluginParty(this));
        require(new PluginPermission(this));
        require(new PluginPortal(this));
        require(new PluginPunish(this));
        require(new PluginReport(this));
        require(new PluginScoreboard(this));

        if (_isDebug)
            log("Core plugins instantiated in " + (System.currentTimeMillis() - start) + "ms.");

        load();

        if (_isDebug)
           log("Local plugins instantiated in " + (System.currentTimeMillis() - start) + "ms.");

        _miniPluginClassMap.values().forEach(miniPlugin -> miniPlugin.load(_miniPluginClassMap));

        if (_isDebug)
          log("Loaded in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public final void onEnable() {
        long start = System.currentTimeMillis();

        enable();
        _miniPluginClassMap.values().forEach(MiniPlugin::enable);

        if (_isDebug)
            log("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public final void onDisable() {
        long start = System.currentTimeMillis();

        disable();
        _miniPluginClassMap.values().forEach(MiniPlugin::disable);
        _miniPluginClassMap.clear();

        if (_isDebug)
          log("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public final void require(MiniPlugin<? extends HexusPlugin> miniPlugin) {
        //noinspection unchecked
        _miniPluginClassMap.put((Class<? extends MiniPlugin<? extends HexusPlugin>>) miniPlugin.getClass(), miniPlugin);
    }

    public final void log(String message) {
        getLogger().info(message);
    }

    public final File getFile() {
        return super.getFile();
    }

}
