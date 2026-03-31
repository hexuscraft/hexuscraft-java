package net.hexuscraft.core.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class CoreCommand extends MiniPlugin<HexusPlugin>
{

    public Set<Command> _commands;
    private final AtomicReference<SimpleCommandMap> _commandMap;

    public CoreCommand(HexusPlugin plugin)
    {
        super(plugin, "Command");

        _commands = new HashSet<>();
        _commandMap = new AtomicReference<>();
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _commandMap.set(((CraftServer) _hexusPlugin.getServer()).getCommandMap());
    }

    @Override
    public void onEnable()
    {
        _commandMap.get().getCommands().forEach(command -> command.setPermissionMessage(F.fInsufficientPermissions()));
    }

    @Override
    public void onDisable()
    {
        _commands.forEach(this::unregister);
        _commands.clear();
    }

    public void register(Command command)
    {
        _commands.add(command);
        _commandMap.get().register("", command);
    }

    public void unregister(Command command)
    {
        command.unregister(_commandMap.get());
    }

}