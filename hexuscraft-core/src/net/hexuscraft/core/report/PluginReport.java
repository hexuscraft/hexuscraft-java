package net.hexuscraft.core.report;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.report.command.CommandReport;

import java.util.Map;

public class PluginReport extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_REPORT
    }

    PluginCommand _pluginCommand;

    public PluginReport(final HexusPlugin plugin) {
        super(plugin, "Reports");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_REPORT);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandReport(this));
    }

}
