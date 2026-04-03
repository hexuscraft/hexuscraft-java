package net.hexuscraft.core.gamemode.command;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.gamemode.CoreGameMode;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandGameMode extends BaseCommand<CoreGameMode>
{

    public CommandGameMode(CoreGameMode coreGameMode)
    {
        super(coreGameMode,
                "gamemode",
                "<Players> [Toggle]",
                "Toggle creative mode.",
                Set.of("gm"),
                CoreGameMode.PERM.COMMAND_GAMEMODE);
    }

    @Override
    public void run(CommandSender sender, String alias, String[] args)
    {
        if (args.length > 2)
        {
            sender.sendMessage(help(alias));
            return;
        }

        Player[] targets;

        if (args.length > 0)
        {
            targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                    args[0],
                    sender,
                    matches -> matches.length == 0);
            if (targets.length == 0)
            {
                return;
            }
        }
        else if (sender instanceof Player player)
        {
            targets = new Player[]{player};
        }
        else
        {
            sender.sendMessage(F.fMain(this, F.fError("Only players can toggle their own creative mode.")));
            return;
        }

        // Require the COMMAND_GAMEMODE_OTHERS permission to toggle Creative Mode of other players
        if (!sender.hasPermission(CoreGameMode.PERM.COMMAND_GAMEMODE_OTHERS.name()))
        {
            if (targets.length > 1)
            {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            for (Player target : targets)
            {
                if (target.equals(sender))
                {
                    continue;
                }
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }
        }

        GameMode newGameMode;

        if (targets.length > 1)
        {
            if (args.length == 1)
            {
                sender.sendMessage(F.fMain(this, F.fError("Toggle argument must be provided with multiple targets.")));
                return;
            }
            newGameMode = getGameModeFromToggle(args[1]);
        }
        else
        {
            newGameMode = getGameModeFromToggle(targets[0].getGameMode().equals(GameMode.CREATIVE) ? "false" : "true");
        }

        if (newGameMode == null)
        {
            sender.sendMessage(F.fMain(this, F.fError("Invalid toggle argument: ", F.fItem(args[1]))));
            return;
        }

        Arrays.stream(targets).forEach(target -> target.setGameMode(newGameMode));
        sender.sendMessage(F.fMain(this,
                F.fItem(Arrays.stream(targets).map(Player::getDisplayName).toArray(String[]::new)),
                " Creative Mode: ",
                F.fBoolean(newGameMode.equals(GameMode.CREATIVE))));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(),
                    sender,
                    true);
        }
        return List.of();
    }

    GameMode getGameModeFromToggle(String toggle)
    {
        if (Arrays.stream(new String[]{"0", "false"}).toList().contains(toggle.toLowerCase()))
        {
            return _miniPlugin._hexusPlugin.getServer().getDefaultGameMode();
        }
        if (Arrays.stream(new String[]{"1", "true"}).toList().contains(toggle.toLowerCase()))
        {
            return GameMode.CREATIVE;
        }
        return null;
    }

}
