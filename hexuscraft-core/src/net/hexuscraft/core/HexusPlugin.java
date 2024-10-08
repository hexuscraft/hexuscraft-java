package net.hexuscraft.core;

import net.hexuscraft.core.anticheat.MiniPluginAntiCheat;
import net.hexuscraft.core.authentication.MiniPluginAuthentication;
import net.hexuscraft.core.buildversion.MiniPluginBuildVersion;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.combat.MiniPluginCombat;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.cooldown.MiniPluginCooldown;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.entity.MiniPluginEntity;
import net.hexuscraft.core.gamemode.MiniPluginGameMode;
import net.hexuscraft.core.item.MiniPluginItem;
import net.hexuscraft.core.party.MiniPluginParty;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.core.punish.MiniPluginPunish;
import net.hexuscraft.core.report.MiniPluginReport;
import net.hexuscraft.core.scoreboard.MiniPluginScoreboard;
import net.hexuscraft.core.teleport.MiniPluginTeleport;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class HexusPlugin extends JavaPlugin implements IHexusPlugin, Listener {

    private final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> _miniPluginClassMap;

    public final boolean _isDebug;

    public HexusPlugin() {
        _miniPluginClassMap = new HashMap<>();
        _isDebug = new File("_debug.dat").isFile() || new File("../_debug.dat").isFile();
    }

    @Override
    public void onLoad() {
        long start = System.currentTimeMillis();

        require(new MiniPluginAntiCheat(this));
        require(new MiniPluginAuthentication(this));
        require(new MiniPluginBuildVersion(this));
        require(new MiniPluginChat(this));
        require(new MiniPluginCombat(this));
        require(new MiniPluginCommand(this));
        require(new MiniPluginCooldown(this));
        require(new MiniPluginDatabase(this));
        require(new MiniPluginEntity(this));
        require(new MiniPluginGameMode(this));
        require(new MiniPluginItem(this));
        require(new MiniPluginParty(this));
        require(new MiniPluginPermission(this));
        require(new MiniPluginPortal(this));
        require(new MiniPluginPunish(this));
        require(new MiniPluginReport(this));
        require(new MiniPluginScoreboard(this));
        require(new MiniPluginTeleport(this));
        debug("Core plugins instantiated in " + (System.currentTimeMillis() - start) + "ms.");

        load();
        debug("Local plugins instantiated in " + (System.currentTimeMillis() - start) + "ms.");

        _miniPluginClassMap.values().forEach(miniPlugin -> miniPlugin.load(_miniPluginClassMap));
        debug("Loaded in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        getServer().getPluginManager().registerEvents(this, this);
        enable();
        _miniPluginClassMap.values().forEach(MiniPlugin::enable);

        debug("Enabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Override
    public void onDisable() {
        long start = System.currentTimeMillis();

        disable();
        _miniPluginClassMap.values().forEach(MiniPlugin::disable);
        _miniPluginClassMap.clear();

        debug("Disabled in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void require(final MiniPlugin<? extends HexusPlugin> miniPlugin) {
        debug("Instantiating " + miniPlugin._name + "...");

        //noinspection unchecked
        _miniPluginClassMap.put((Class<? extends MiniPlugin<? extends HexusPlugin>>) miniPlugin.getClass(), miniPlugin);
    }

    public void log(final String message) {
        getLogger().info(message);
    }

    public void debug(final String message) {
        if (!_isDebug) return;
        log(message);
    }

    public File getFile() {
        return super.getFile();
    }

}
