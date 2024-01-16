package net.hexuscraft.core.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginCommand extends MiniPlugin<HexusPlugin> {

    private Set<Command> _commands;
    private SimpleCommandMap _commandMap;

    public PluginCommand(final HexusPlugin plugin) {
        super(plugin, "Command");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _commands = new HashSet<>();
        _commandMap = ((CraftServer) _plugin.getServer()).getCommandMap();
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