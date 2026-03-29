package net.hexuscraft.core.command;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class BaseCommand<T extends MiniPlugin<? extends HexusPlugin>> extends Command
{

    public T _miniPlugin;

    public BaseCommand(T miniPlugin,
            String name,
            String usage,
            String description,
            Set<String> aliases,
            IPermission permission)
    {
        super(name.toLowerCase(), description, usage, aliases.stream().map(String::toLowerCase).toList());

        setPermission(permission.toString());
        setPermissionMessage(F.fInsufficientPermissions());

        _miniPlugin = miniPlugin;
    }

    public String help(String alias)
    {
        return F.fMain(this, "Command Usage:\n", F.fCommand(alias, getUsage(), getDescription()));
    }

    public boolean isAlias(String alias)
    {
        return getName().equalsIgnoreCase(alias) ||
                getAliases().stream().map(String::toLowerCase).toList().contains(alias.toLowerCase());
    }

    public void run(CommandSender sender, String alias, String[] args)
    {
        sender.sendMessage(F.fMain(this,
                F.fError("This command has no implementation. Please contact a staff member if this issue persists.")));
    }

    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        return List.of();
    }

    @Override
    public String toString()
    {
        return _miniPlugin.toString();
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args)
    {
        if (!testPermission(sender))
        {
            return true;
        }
        try
        {
            run(sender, alias, args);
        }
        catch (Throwable ex)
        {
            _miniPlugin.logInfo("An exception occurred while CommandSender '" +
                    sender.getName() +
                    "' executing BaseCommand '" +
                    alias +
                    " " +
                    String.join(" ", args) +
                    "':" +
                    ex.getMessage() +
                    "\n> " +
                    String.join("\n",
                            Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new)));
            sender.sendMessage(F.fMain(this,
                    F.fError("An unknown error occurred while executing this command. Please try again later.")));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args)
    {
        if (!testPermission(sender))
        {
            return List.of();
        }

        return tab(sender, alias, args).stream()
                .filter(Objects::nonNull)
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

}