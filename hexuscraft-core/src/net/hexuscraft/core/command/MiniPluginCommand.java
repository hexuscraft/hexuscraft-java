package net.hexuscraft.core.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class MiniPluginCommand extends MiniPlugin<HexusPlugin> {

    private final Set<Command> _commands;
    private final AtomicReference<SimpleCommandMap> _commandMap;

    public MiniPluginCommand(final HexusPlugin plugin) {
        super(plugin, "Command");

        _commands = new HashSet<>();
        _commandMap = new AtomicReference<>();
    }

    @Override
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _commandMap.set(((CraftServer) _hexusPlugin.getServer()).getCommandMap());
    }

    @Override
    public void onEnable() {
        _commandMap.get().getCommands().forEach(command -> command.setPermissionMessage(F.fInsufficientPermissions()));
    }

    @Override
    public void onDisable() {
        _commands.forEach(this::unregister);
        _commands.clear();
    }

    public void register(final Command command) {
        _commands.add(command);
        _commandMap.get().register("", command);
    }

    public void unregister(final Command command) {
        command.unregister(_commandMap.get());
    }

}