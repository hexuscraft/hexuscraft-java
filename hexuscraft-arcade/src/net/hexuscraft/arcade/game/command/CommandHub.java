package net.hexuscraft.arcade.game.command;

import net.hexuscraft.arcade.game.MiniPluginGame;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandHub extends BaseCommand<MiniPluginGame> {

    private final MiniPluginPortal _miniPluginPortal;

    public CommandHub(final MiniPluginGame miniPluginGame, final MiniPluginPortal miniPluginPortal) {
        super(miniPluginGame, "hub", "", "Teleport back to a lobby server.", Set.of("lobby"), MiniPluginGame.PERM.COMMAND_HUB);
        _miniPluginPortal = miniPluginPortal;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(F.fMain(this, "Only players can execute this command."));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> _miniPluginPortal.teleportPlayerToRandomServer(player, "Lobby"));
    }

}
