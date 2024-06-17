package net.hexuscraft.core.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public abstract class BaseMultiCommand<T extends MiniPlugin<? extends HexusPlugin>> extends BaseCommand<T> {

    private final Set<BaseCommand<T>> _commands;

    public BaseMultiCommand(final T miniPlugin, String name, String description, Set<String> aliases, IPermission permission, Set<BaseCommand<T>> commands) {
        super(miniPlugin, name, "", description, aliases, permission);
        _commands = commands;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            for (final BaseCommand<? extends MiniPlugin<? extends HexusPlugin>> command : _commands) {
                if (!command.isAlias(args[0])) continue;
                if (!command.testPermission(sender)) return;
                command.run(sender, alias + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(help(alias));

        _commands.stream().sorted(Comparator.comparing(Command::getName)).forEach(command -> {
            if (!command.testPermissionSilent(sender)) return;
            builder.append("\n").append(F.fCommand(alias + " " + command.getName(), command));
        });

        sender.sendMessage(builder.toString());
    }

    @Override
    public final List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            for (final BaseCommand<? extends MiniPlugin<? extends HexusPlugin>> command : _commands) {
                if (!command.getName().equals(args[0]) && !command.getAliases().contains(args[0])) continue;
                if (!command.testPermissionSilent(sender)) break;

                return command.tab(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            }
            return List.of();
        }

        List<String> completes = new ArrayList<>();
        _commands.forEach(commandBase -> {
            if (!commandBase.testPermissionSilent(sender)) return;
            completes.add(commandBase.getName());
            completes.addAll(commandBase.getAliases());
        });
        return completes;
    }

}