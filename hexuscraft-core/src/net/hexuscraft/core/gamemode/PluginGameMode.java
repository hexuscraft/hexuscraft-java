package net.hexuscraft.core.gamemode;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.gamemode.command.CommandGameMode;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class PluginGameMode extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_GAMEMODE,
        COMMAND_GAMEMODE_OTHERS
    }

    PluginCommand _pluginCommand;

    public PluginGameMode(JavaPlugin javaPlugin) {
        super(javaPlugin, "Game Mode");

        PermissionGroup.BUILDER._permissions.add(PERM.COMMAND_GAMEMODE);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_GAMEMODE_OTHERS);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandGameMode(this));
    }

}
