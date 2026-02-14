package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;

import java.util.Set;

public final class CommandLocatePlayer extends BaseCommand<MiniPluginPortal> {

    public CommandLocatePlayer(final MiniPluginPortal miniPluginPortal) {
        super(miniPluginPortal,
                "player",
                "<Name>",
                "Locate a specific player's server.",
                Set.of("plr",
                        "p"),
                MiniPluginPortal.PERM.COMMAND_LOCATE_PLAYER);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {

    }

}
