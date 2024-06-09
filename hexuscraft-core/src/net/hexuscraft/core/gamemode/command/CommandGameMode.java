package net.hexuscraft.core.gamemode.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.gamemode.PluginGameMode;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandGameMode extends BaseCommand<HexusPlugin> {

    public CommandGameMode(PluginGameMode pluginGameMode) {
        super(pluginGameMode, "gamemode", "[Player]", "Toggle creative mode.", Set.of("gm"), PluginGameMode.PERM.COMMAND_GAMEMODE);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        Player target;

        if (args.length == 1) {
            final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._plugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length != 1) return;
            target = targets[0];
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(F.fMain(this) + "Only players can toggle their own creative mode.");
            return;
        }

        if (target != sender && !sender.hasPermission(PluginGameMode.PERM.COMMAND_GAMEMODE_OTHERS.name())) {
            sender.sendMessage(F.fInsufficientPermissions());
            return;
        }

        final GameMode newGameMode = target.getGameMode() != GameMode.CREATIVE ? GameMode.CREATIVE : _miniPlugin._plugin.getServer().getDefaultGameMode();
        target.setGameMode(newGameMode);

        sender.sendMessage(F.fMain(this) + F.fItem(target) + " Creative Mode: " + F.fBoolean(newGameMode.equals(GameMode.CREATIVE)));

        if (target == sender) return;
        target.sendMessage(F.fMain(this) + F.fItem(sender) + " set your Creative Mode: " + F.fBoolean(newGameMode.equals(GameMode.CREATIVE)));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        //noinspection ReassignedVariable
        Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._plugin.getServer().getOnlinePlayers().stream();
        if (sender instanceof Player player) {
            streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
        }
        return streamedOnlinePlayers.map(Player::getName).toList();
    }

}
