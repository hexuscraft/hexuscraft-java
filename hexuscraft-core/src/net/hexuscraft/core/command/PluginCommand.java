package net.hexuscraft.core.command;

import net.hexuscraft.core.MiniPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginCommand extends MiniPlugin {

    public CommandMap _commandMap;
    Set<Command> _commands;

    public PluginCommand(JavaPlugin javaPlugin) {
        super(javaPlugin, "Command");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _commandMap = ((CraftServer) _javaPlugin.getServer()).getCommandMap();
        _commands = new HashSet<>();
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

    @EventHandler
    public void onChatTab(PlayerChatTabCompleteEvent event) {
        event.getPlayer().sendMessage(event.getTabCompletions().toString());
    }

}