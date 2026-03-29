package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.chat.CoreChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.command.CoreCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandHelp extends BaseCommand<CoreChat>
{

    private final CoreCommand _coreCommand;

    public CommandHelp(CoreChat coreChat, CoreCommand coreCommand)
    {
        super(coreChat, "help", "", "Need some help? We got you covered.", Set.of("?"), CoreChat.PERM.COMMAND_HELP);
        _coreCommand = coreCommand;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 1)
        {
            sender.sendMessage(help(alias));
            return;
        }

        if (args.length == 1)
        {
            Command[] availableCommands = _coreCommand._commands.stream()
                    .filter(command -> command.testPermissionSilent(sender))
                    .sorted(Comparator.comparing(command -> command.getName().toLowerCase()))
                    .toArray(Command[]::new);

            int pageAmount = (int) Math.ceil((double) availableCommands.length / 8);

            int pageNumber;
            try
            {
                pageNumber = Integer.parseInt(args[0]);
                if (pageNumber < 1)
                {
                    throw new NumberFormatException();
                }
                if (pageNumber > pageAmount)
                {
                    throw new NumberFormatException();
                }
            }
            catch (NumberFormatException ex)
            {
                sender.sendMessage(F.fMain(this, F.fError("Invalid page number ", F.fItem(args[0]), ".")));
                return;
            }

            List<String> commandStrings = new ArrayList<>();

            Arrays.stream(availableCommands)
                    .skip((pageNumber - 1) * 8L)
                    .limit(8)
                    .map(command -> F.fCommand(command.getName(), command.getUsage(), command.getDescription()))
                    .forEach(commandStrings::add);

            sender.sendMessage(F.fMain(this, "Commands Page ", F.fItem(pageNumber + " / " + pageAmount), ":") +
                    "\n" +
                    String.join("\n", commandStrings));

            return;
        }

        sender.sendMessage(F.fMain(this, "Hey there! Need some help?") +
                "\n" +
                String.join("\n", constructHelpStrings(alias)));
    }

    private String[] constructHelpStrings(String alias)
    {
        return new String[]{F.fMain("", "Visit our website: " + C.cGreen + "https://hexuscraft.net"),
                F.fMain("", "Join our Discord: " + C.cPurple + "https://discord.gg/yusJMxrg3e"),
                F.fMain("", "Request help from staff with ", F.fItem("/support"), "."),
                F.fMain("", "Type ", F.fItem("/" + alias + " 1"), " for a list of commands.")};

    }

}