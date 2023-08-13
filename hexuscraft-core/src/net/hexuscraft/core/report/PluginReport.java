package net.hexuscraft.core.report;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.report.command.CommandReport;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginReport extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_REPORT
    }

    PluginCommand _pluginCommand;

    public PluginReport(JavaPlugin javaPlugin) {
        super(javaPlugin, "Reports");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandReport(this));
    }

}
