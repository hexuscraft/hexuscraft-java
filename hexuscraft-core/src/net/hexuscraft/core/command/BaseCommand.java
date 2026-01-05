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

public abstract class BaseCommand<T extends MiniPlugin<? extends HexusPlugin>> extends Command {

    public final T _miniPlugin;

    public BaseCommand(final T miniPlugin, final String name, final String usage, final String description,
                       final Set<String> aliases, final IPermission permission) {
        super(name.toLowerCase(), description, usage, aliases.stream().map(String::toLowerCase).toList());

        setPermission(permission.toString());
        setPermissionMessage(F.fInsufficientPermissions());

        _miniPlugin = miniPlugin;
    }

    public String help(final String alias) {
        return F.fMain(this, "Command Usage:\n", F.fCommand(alias, getUsage(), getDescription()));
    }

    public final boolean isAlias(final String alias) {
        return getName().equalsIgnoreCase(alias) ||
                getAliases().stream().map(String::toLowerCase).toList().contains(alias.toLowerCase());
    }

    public void run(final CommandSender sender, final String alias, final String[] args) {
        sender.sendMessage(F.fMain(this,
                F.fError("This command has no implementation. Please contact a staff member if this issue persists.")));
    }

    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return List.of();
    }

    @Override
    public String toString() {
        return _miniPlugin.toString();
    }

    @Override
    public final boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        try {
            run(sender, alias, args);
        } catch (final Exception ex) {
            _miniPlugin.logInfo(
                    "An exception occurred while CommandSender '" + sender.getName() + "' executing BaseCommand '" +
                            alias + " " + String.join(" ", args) + "':" + ex.getMessage() + "\n> " + String.join("\n",
                            Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).toArray(String[]::new)));
            sender.sendMessage(F.fMain(this,
                    F.fError("An unknown error occurred while executing this command. Please try again later.")));
        }
        return true;
    }

    @Override
    public final List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        if (!testPermission(sender)) return List.of();

        return tab(sender, alias, args).stream().filter(Objects::nonNull)
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

}