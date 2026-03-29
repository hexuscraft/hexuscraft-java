package net.hexuscraft.arcade.manager.command;

import net.hexuscraft.arcade.manager.ArcadeManager;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.portal.CorePortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandHub extends BaseCommand<ArcadeManager>
{

    private final CorePortal _corePortal;

    public CommandHub(ArcadeManager arcadeManager, CorePortal corePortal)
    {
        super(arcadeManager,
                "hub",
                "",
                "Teleport back to a lobby server.",
                Set.of("lobby"),
                ArcadeManager.PERM.COMMAND_HUB);
        _corePortal = corePortal;
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (!(sender instanceof Player player))
        {
            sender.sendMessage(F.fMain(this, "Only players can execute this command."));
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> _corePortal.teleportPlayerToRandomServer(player, "Lobby"));
    }

}
