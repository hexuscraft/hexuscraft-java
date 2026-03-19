package net.hexuscraft.core.report;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.report.command.CommandReport;

import java.util.Map;

public final class CoreReport extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_REPORT
    }

    CoreCommand _pluginCommand;

    public CoreReport(final HexusPlugin plugin) {
        super(plugin,
                "Reports");

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_REPORT);
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandReport(this));
    }

}
