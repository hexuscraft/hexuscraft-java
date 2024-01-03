package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandSend extends BaseCommand {

    private final PluginPortal _portal;

    public CommandSend(final PluginPortal pluginPortal) {
        super(pluginPortal, "send", "<Player> <Name>", "Teleport a player to a server.", Set.of(), PluginPortal.PERM.COMMAND_SEND);

        _portal = pluginPortal;
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length != 2) {
            sender.sendMessage(help(alias));
            return;
        }

        final String targetName = args[0];
        final String serverName = args[1];

        if (PlayerSearch.fetchMojangProfile(targetName, sender) == null) return;

        if (!_portal.doesServerExistWithName(serverName)) {
            sender.sendMessage(F.fMain(this) + F.fError("Could not locate a server with name ", F.fItem(serverName), "."));
            return;
        }

        sender.sendMessage(F.fMain(this) + "Sending " + F.fItem(targetName) + " to server " + F.fItem(serverName) + ".");
        _portal.teleport(targetName, serverName, sender.getName());
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        final List<String> names = new ArrayList<>();
        if (args.length == 1) {
            //noinspection ReassignedVariable
            Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream();
            if (sender instanceof final Player player) {
                streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
            }

            names.addAll(List.of("*", "**"));
            names.addAll(streamedOnlinePlayers.map(Player::getName).toList());
        }
        return names;
    }

}
