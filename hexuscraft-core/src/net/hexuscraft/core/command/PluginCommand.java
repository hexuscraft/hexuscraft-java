package net.hexuscraft.core.command;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
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
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        String[] messageArray = event.getMessage().split(" ");

        String alias = messageArray[0].split("/", 1)[1];
        //noinspection unused
        String[] args = messageArray.length > 1 ? Arrays.copyOfRange(messageArray, 1, messageArray.length) : new String[0];

        for (Command command : ((SimpleCommandMap) _commandMap).getCommands()) {
            boolean isAlias = command.getName().equalsIgnoreCase(alias) || command.getAliases().stream().map(String::toLowerCase).toList().contains(alias.toLowerCase());
            if (!isAlias || !command.testPermissionSilent(player)) {
                event.setCancelled(true);
                player.sendMessage(F.fMain(this) + "Unknown command. Type " + F.fItem("/help") + " for help.");
                return;
            }
        }
        ;
    }

}