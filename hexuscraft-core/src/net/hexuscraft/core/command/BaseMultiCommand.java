package net.hexuscraft.core.command;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.permission.IPermission;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BaseMultiCommand extends BaseCommand {

    private final Set<BaseCommand> _commands;

    public BaseMultiCommand(MiniPlugin miniPlugin, String name, String description, Set<String> aliases, IPermission permission, Set<BaseCommand> commands) {
        super(miniPlugin, name, "", description, aliases, permission);

        _commands = commands;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 0) {
            for (BaseCommand command : _commands) {
                if (!command.isAlias(args[0])) {
                    continue;
                }
                if (!command.testPermission(sender)) {
                    return;
                }

                command.run(sender, alias + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
                return;
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(help(alias));
        for (BaseCommand command : _commands) {
            if (!command.testPermissionSilent(sender)) {
                continue;
            }
            builder.append("\n").append(F.fCommand(alias + " " + command.getName(), command));
        }
        sender.sendMessage(builder.toString());
    }

    @Override
    public final List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            for (BaseCommand command : _commands) {
                if (!command.getName().equals(args[0]) && !command.getAliases().contains(args[0])) {
                    continue;
                }
                if (!command.testPermissionSilent(sender)) {
                    break;
                }

                return command.tab(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            }
            return List.of();
        }

        List<String> completes = new ArrayList<>();
        _commands.forEach(commandBase -> {
            if (!commandBase.testPermissionSilent(sender)) {
                return;
            }
            completes.add(commandBase.getName());
            completes.addAll(commandBase.getAliases());
        });
        return completes;
    }

}