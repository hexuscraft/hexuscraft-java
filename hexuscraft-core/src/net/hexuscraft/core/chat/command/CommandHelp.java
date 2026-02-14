package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.command.MiniPluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.Comparator;
import java.util.Set;

public final class CommandHelp extends BaseCommand<MiniPluginChat> {

    private final MiniPluginCommand _miniPluginCommand;

    public CommandHelp(final MiniPluginChat miniPluginChat, final MiniPluginCommand miniPluginCommand) {
        super(miniPluginChat,
                "help",
                "",
                "Need some help? We got you covered.",
                Set.of("?"),
                MiniPluginChat.PERM.COMMAND_HELP);
        _miniPluginCommand = miniPluginCommand;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        if (args.length == 1) {
            final Command[] availableCommands = ((CraftServer) _miniPlugin._hexusPlugin.getServer()).getCommandMap()
                    .getCommands()
                    .stream()
                    .filter(command -> command.testPermissionSilent(sender))
                    .sorted(Comparator.comparing(command -> command.getName()
                            .toLowerCase()))
                    .toArray(Command[]::new);

            final int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[0]);
                if (pageNumber < 1) throw new NumberFormatException();
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this,
                        F.fError("Invalid page number ",
                                F.fItem(args[0]),
                                ". Expected a positive, non-zero integer.")));
                return;
            }

            sender.sendMessage(F.fMain(this,
                    "Commands Page ",
                    F.fItem(pageNumber + " / " + Math.ceil((double) availableCommands.length / 8))));
            return;
        }

        sender.sendMessage(F.fMain(this,
                "Hey there! Need some help?",
                String.join("\n",
                        constructHelpStrings(alias))));
    }

    private String[] constructHelpStrings(final String alias) {
        return new String[]{F.fMain("",
                "Visit our website: " + C.cGreen + "https://hexuscraft.net"), F.fMain("",
                "Join our Discord: " + C.cPurple + "https://discord.gg/yusJMxrg3e"), F.fMain("",
                "Request help from staff with ",
                F.fItem("/support"),
                "."), F.fMain("",
                "Type ",
                F.fItem("/" + alias + " 1"),
                " for a list of commands.")};

    }

}