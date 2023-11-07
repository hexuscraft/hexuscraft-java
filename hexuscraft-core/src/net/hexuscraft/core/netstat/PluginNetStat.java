package net.hexuscraft.core.netstat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.netstat.command.CommandNetStat;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginNetStat extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_NETSTAT,
        COMMAND_NETSTAT_GROUP,
        COMMAND_NETSTAT_GROUP_DELETE,
        COMMAND_NETSTAT_SERVER
    }

    public PluginNetStat(JavaPlugin javaPlugin) {
        super(javaPlugin, "Network Statistics");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_GROUP_DELETE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_NETSTAT_SERVER);
    }

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandNetStat(this, _pluginDatabase));
    }

}
