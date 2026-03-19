package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandLocateServer extends BaseCommand<CorePortal> {

    public CommandLocateServer(final CorePortal corePortal) {
        super(corePortal,
                "server",
                "<Name>",
                "List all players in a specific server.",
                Set.of("srv",
                        "s"),
                CorePortal.PERM.COMMAND_LOCATE_SERVER);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        super.run(sender,
                alias,
                args);
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1)
            return Arrays.asList(_miniPlugin.getServerNames());
        return List.of();
    }

}
