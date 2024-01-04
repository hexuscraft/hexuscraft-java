package net.hexuscraft.core.command;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseCommand extends Command {

    public final MiniPlugin _miniPlugin;
    public final IPermission _permission;

    protected BaseCommand(final MiniPlugin miniPlugin, final String name, final String usage, final String description, final Set<String> aliases, final IPermission permission) {
        super(name.toLowerCase(), description, usage, aliases.stream().map(String::toLowerCase).toList());
        setPermission(permission.toString());
        setPermissionMessage(F.fInsufficientPermissions());

        _miniPlugin = miniPlugin;
        _permission = permission;
    }

    public String help(final String alias) {
        return F.fMain(_miniPlugin._name) + "Command Usage:\n" + F.fCommand(alias, this);
    }

    public final boolean isAlias(final String alias) {
        return getName().equalsIgnoreCase(alias) || getAliases().contains(alias.toLowerCase());
    }

    public void run(final CommandSender sender, final String alias, final String[] args) {
    }

    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return List.of();
    }

    @Override
    public final boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        run(sender, alias, args);
        return true;
    }

    @Override
    public final List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (!testPermission(sender)) {
            return List.of();
        }

        List<String> completes = new ArrayList<>();
        tab(sender, alias, args).forEach(s -> {
            if (s == null) {
                return;
            } // allows using stream.map() to nullify unwanted completions
            if (!s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                return;
            }
            completes.add(s);
        });
        return completes;
    }

}