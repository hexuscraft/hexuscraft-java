package net.hexuscraft.core.netstat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.netstat.command.CommandNetStat;
import net.hexuscraft.core.permission.IPermission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginNetStat extends MiniPlugin {

    public PluginNetStat(JavaPlugin javaPlugin) {
        super(javaPlugin, "NetStat");
    }

    public enum PERM implements IPermission {
        COMMAND_NETSTAT,
        COMMAND_NETSTAT_GROUP,
        COMMAND_NETSTAT_SERVER
    }

    private PluginCommand _pluginCommand;

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandNetStat(this));
    }

}
