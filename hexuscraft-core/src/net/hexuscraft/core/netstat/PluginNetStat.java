package net.hexuscraft.core.netstat;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.command.CommandNetStat;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;

import java.util.Map;

public class PluginNetStat extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_NETSTAT,
        COMMAND_NETSTAT_GROUP,
        COMMAND_NETSTAT_GROUP_CREATE,
        COMMAND_NETSTAT_GROUP_DELETE,
        COMMAND_NETSTAT_GROUP_LIST,
        COMMAND_NETSTAT_SERVER
    }

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;

    public PluginNetStat(final HexusPlugin plugin) {
        super(plugin, "Network Statistics");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP_CREATE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP_DELETE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP_LIST);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_SERVER);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandNetStat(this, _pluginDatabase));
    }

}
