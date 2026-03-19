package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.ArcadeGame;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandHub extends BaseCommand<ArcadeGame> {

    private final CorePortal _corePortal;

    public CommandHub(final ArcadeGame arcadeGame, final CorePortal corePortal) {
        super(arcadeGame,
                "hub",
                "",
                "Teleport back to a lobby server.",
                Set.of("lobby"),
                ArcadeGame.PERM.COMMAND_HUB);
        _corePortal = corePortal;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this,
                    "Only players can execute this command."));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> _corePortal.teleportPlayerToRandomServer(player,
                "Lobby"));
    }

}
