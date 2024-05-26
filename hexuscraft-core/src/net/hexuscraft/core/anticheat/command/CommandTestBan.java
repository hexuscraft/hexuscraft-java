package net.hexuscraft.core.anticheat.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.anticheat.PluginAntiCheat;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandTestBan extends BaseCommand<HexusPlugin> {

    private final PluginAntiCheat _antiCheat;

    public CommandTestBan(final PluginAntiCheat antiCheat) {
        super(antiCheat, "testhacban", "<Player>",
                "Test the HAC ban sequence on a player. (doesn't actually ban them)",
                Set.of(),
                PluginAntiCheat.PERM.COMMAND_TESTBAN);
        _antiCheat = antiCheat;
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        final Player[] targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._plugin.getServer().getOnlinePlayers(), args[0], sender);
        if (targets.length != 1) return;

        final Player target = targets[0];
        sender.sendMessage(F.fMain(this) + "Performing a test ban against " + F.fItem(target));
        _antiCheat.animation(target, "Test Hacking Ban");
    }

    @Override
    public final List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            return List.of();
        }

        final Stream<? extends Player> streamedOnlinePlayers;
        if (sender instanceof Player player) {
            streamedOnlinePlayers = _miniPlugin._plugin.getServer()
                    .getOnlinePlayers()
                    .stream()
                    .filter(p -> p.canSee(player));
        } else {
            streamedOnlinePlayers = _miniPlugin._plugin.getServer()
                    .getOnlinePlayers()
                    .stream();
        }

        return streamedOnlinePlayers.map(Player::getName).toList();
    }

}
