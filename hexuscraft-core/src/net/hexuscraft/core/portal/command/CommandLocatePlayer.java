package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class CommandLocatePlayer extends BaseCommand<CorePortal>
{

    public CommandLocatePlayer(CorePortal corePortal)
    {
        super(corePortal,
              "player",
              "<Name>",
              "Locate a specific player's server.",
              Set.of("plr", "p"),
              CorePortal.PERM.COMMAND_LOCATE_PLAYER);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {

    }

}
