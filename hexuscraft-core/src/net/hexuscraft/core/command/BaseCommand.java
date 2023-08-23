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

    protected BaseCommand(MiniPlugin miniPlugin, String name, String usage, String description, Set<String> aliases, IPermission permission) {
        super(name.toLowerCase(), description, usage, aliases.stream().map(String::toLowerCase).toList());
        setPermission(permission.toString());
        setPermissionMessage(F.fInsufficientPermissions());

        _miniPlugin = miniPlugin;
        _permission = permission;
    }

    public String help(String alias) {
        return F.fMain(_miniPlugin._name) + "Command Usage:\n" + F.fCommand(this, alias);
    }

    public final boolean isAlias(String alias) {
        return getName().equalsIgnoreCase(alias) || getAliases().contains(alias.toLowerCase());
    }

    public void run(CommandSender sender, String alias, String[] args) {
    }

    public List<String> tab(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    @Override
    public final boolean execute(CommandSender sender, String alias, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        run(sender, alias, args);
        return true;
    }

    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) {
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