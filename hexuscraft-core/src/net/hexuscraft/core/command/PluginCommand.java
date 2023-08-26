package net.hexuscraft.core.command;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginCommand extends MiniPlugin {

    private Set<Command> _commands;
    private SimpleCommandMap _commandMap;

    public PluginCommand(JavaPlugin javaPlugin) {
        super(javaPlugin, "Command");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _commands = new HashSet<>();
        _commandMap = ((CraftServer) _javaPlugin.getServer()).getCommandMap();
    }

    @Override
    public void onEnable() {
        _commandMap.getCommands().forEach(command -> command.setPermissionMessage(F.fInsufficientPermissions()));
    }

    @Override
    public void onDisable() {
        for (Command command : _commands) {
            command.unregister(_commandMap);
        }
        _commands = null;
        _commandMap = null;
    }

    public void register(Command command) {
        _commandMap.register("_", command);
    }

}