package net.hexuscraft.core.gamemode.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.gamemode.PluginGameMode;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandGameMode extends BaseCommand {

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
            final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length != 1) {
                return;
            }
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

        boolean toggleCreativeMode = target.getGameMode() != GameMode.CREATIVE;
        target.setGameMode(toggleCreativeMode ? GameMode.CREATIVE : _miniPlugin._javaPlugin.getServer().getDefaultGameMode());

        sender.sendMessage(F.fMain(this) + F.fItem(target) + " Creative Mode: " + F.fBoolean(toggleCreativeMode));

        if (target == sender) {
            return;
        }

        target.sendMessage(F.fMain(this) + F.fItem(sender) + " set your Creative Mode: " + F.fBoolean(toggleCreativeMode));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        List<String> names = new ArrayList<>();
        if (args.length == 1) {
            _miniPlugin._javaPlugin.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
        }
        return names;
    }

}
