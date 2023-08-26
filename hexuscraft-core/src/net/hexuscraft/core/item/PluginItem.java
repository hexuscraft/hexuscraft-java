package net.hexuscraft.core.item;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.item.command.CommandClear;
import net.hexuscraft.core.item.command.CommandGive;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginItem extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_GIVE,
        COMMAND_CLEAR
    }

    PluginCommand _pluginCommand;

    public PluginItem(JavaPlugin javaPlugin) {
        super(javaPlugin, "Item");

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GIVE);
        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_CLEAR);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandClear(this));
        _pluginCommand.register(new CommandGive(this));
    }

}
