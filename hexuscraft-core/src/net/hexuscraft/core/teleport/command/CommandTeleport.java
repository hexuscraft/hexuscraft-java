package net.hexuscraft.core.teleport.command;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.teleport.PluginTeleport;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandTeleport extends BaseCommand<HexusPlugin> {

    public CommandTeleport(final PluginTeleport pluginTeleport) {
        super(pluginTeleport, "teleport", "[Players] <Player/X Y Z>", "Teleport one or more players to a player or coordinates", Set.of("tp"), PluginTeleport.PERM.COMMAND_TELEPORT);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        sender.sendMessage("Work in progress");
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            final List<String> names = new ArrayList<>(List.of("*", "**"));
            if (sender instanceof Player player) {
                names.add(".");
                names.addAll(_miniPlugin._plugin.getServer().getOnlinePlayers().stream().filter(target -> target.canSee(player)).map(Player::getName).toList());
                return names;
            }

            names.addAll(_miniPlugin._plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
            return names;
        }

        if (args.length == 2) {
            final List<String> names = new ArrayList<>(List.of("*", "**"));
            if (sender instanceof Player player) {
                names.add(".");
                names.add(Integer.toString(player.getLocation().getBlockZ()));
                names.addAll(_miniPlugin._plugin.getServer().getOnlinePlayers().stream().filter(target -> target.canSee(player)).map(Player::getName).toList());
                return names;
            }

            return _miniPlugin._plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }

        if (args.length == 3 && sender instanceof Player player)
            return List.of(Integer.toString(player.getLocation().getBlockY()));

        if (args.length == 4 && sender instanceof Player player)
            return List.of(Integer.toString(player.getLocation().getBlockZ()));

        return List.of();
    }

}
