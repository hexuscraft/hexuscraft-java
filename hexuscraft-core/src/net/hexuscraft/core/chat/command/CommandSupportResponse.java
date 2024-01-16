package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.PluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CommandSupportResponse extends BaseCommand {

    final PluginPermission pluginPermission;

    public CommandSupportResponse(PluginChat pluginChat, PluginPermission pluginPermission) {
        super(pluginChat, "supportresponse", "<Player> <Message>", "Respond to a help request.", Set.of("ma", "sr"), PluginChat.PERM.COMMAND_SUPPORT_RESPONSE);
        this.pluginPermission = pluginPermission;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            Player target = _miniPlugin._plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(F.fMain(this) + "Could not find a player with specified name.");
                return;
            }

            final PermissionGroup permissionGroup = sender instanceof Player ? pluginPermission._primaryGroupMap.get((Player) sender) : null;

            for (Player player : _miniPlugin._plugin.getServer().getOnlinePlayers()) {
                if (player.equals(sender) || player.equals(target) || player.hasPermission(PermissionGroup.TRAINEE.toString())) {
                    final PermissionGroup targetGroup = pluginPermission._primaryGroupMap.get(player);

                    final String sourceStr = F.fPermissionGroup(permissionGroup) + " " + sender.getName();
                    final String targetStr = F.fPermissionGroup(targetGroup) + " " + player.getName();

                    player.sendMessage(sourceStr + C.fReset + C.cPurple + " -> " + C.fReset + targetStr + C.fReset + " " + C.cPurple + String.join(" ", Arrays.stream(args).toList().subList(1, args.length)));
                    if (player.equals(target)) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, Integer.MAX_VALUE, 2);
                    }
                }
            }

            return;
        }

        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            return List.of();
        }

        //noinspection ReassignedVariable
        Stream<? extends Player> streamedOnlinePlayers = _miniPlugin._plugin.getServer().getOnlinePlayers().stream();
        if (sender instanceof Player player) {
            streamedOnlinePlayers = streamedOnlinePlayers.filter(p -> p.canSee(player));
        }

        return new ArrayList<>(streamedOnlinePlayers.map(Player::getName).toList());
    }

}
