package net.hexuscraft.core.gamemode.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.gamemode.MiniPluginGameMode;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandGameMode extends BaseCommand<MiniPluginGameMode> {

    public CommandGameMode(final MiniPluginGameMode miniPluginGameMode) {
        super(miniPluginGameMode, "gamemode", "<Players> [Toggle]", "Toggle creative mode.", Set.of("gm"), MiniPluginGameMode.PERM.COMMAND_GAMEMODE);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final Player[] targets;

        if (args.length > 0) {
            targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length == 0) {
                sender.sendMessage(F.fMatches(targets, args[0]));
                return;
            }
        } else if (sender instanceof Player player) {
            targets = new Player[]{player};
        } else {
            sender.sendMessage(F.fMain(this, F.fError("Only players can toggle their own creative mode.")));
            return;
        }

        // Require the COMMAND_GAMEMODE_OTHERS permission to toggle Creative Mode of other players
        if (!sender.hasPermission(MiniPluginGameMode.PERM.COMMAND_GAMEMODE_OTHERS.name())) {
            if (targets.length > 1) {
                sender.sendMessage(F.fInsufficientPermissions(MiniPluginGameMode.PERM.COMMAND_GAMEMODE_OTHERS));
                return;
            }

            for (final Player target : targets) {
                if (target.equals(sender)) continue;
                sender.sendMessage(F.fInsufficientPermissions(MiniPluginGameMode.PERM.COMMAND_GAMEMODE_OTHERS));
                return;
            }
        }

        final GameMode newGameMode;

        if (targets.length > 1) {
            if (args.length == 1) {
                sender.sendMessage(F.fMain(this, F.fError("Toggle argument must be provided with multiple targets.")));
                return;
            }
            newGameMode = getGameModeFromToggle(args[1]);
        } else {
            newGameMode = getGameModeFromToggle(targets[0].getGameMode().equals(GameMode.CREATIVE) ? "false" : "true");
        }

        if (newGameMode == null) {
            sender.sendMessage(F.fMain(this, F.fError("Invalid toggle argument: ", F.fItem(args[1]))));
            return;
        }

        Arrays.stream(targets).forEach(target -> target.setGameMode(newGameMode));
        sender.sendMessage(F.fMain(this, F.fList(targets), " Creative Mode: ", F.fBoolean(newGameMode.equals(GameMode.CREATIVE))));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender);
    }

    private GameMode getGameModeFromToggle(final String toggle) {
        if (Arrays.stream(new String[]{"0", "false"}).toList().contains(toggle.toLowerCase())) {
            return _miniPlugin._hexusPlugin.getServer().getDefaultGameMode();
        }
        if (Arrays.stream(new String[]{"1", "true"}).toList().contains(toggle.toLowerCase())) {
            return GameMode.CREATIVE;
        }
        return null;
    }

}
