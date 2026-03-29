package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandLocatePlayer extends BaseCommand<CorePortal>
{

    public CommandLocatePlayer(final CorePortal corePortal)
    {
        super(corePortal,
              "player",
              "<Name>",
              "Locate a specific player's server.",
              Set.of("plr", "p"),
              CorePortal.PERM.COMMAND_LOCATE_PLAYER);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args)
    {

    }

}
