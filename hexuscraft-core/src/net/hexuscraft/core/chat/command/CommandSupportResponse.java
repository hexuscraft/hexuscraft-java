package net.hexuscraft.core.chat.command;

import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.PlayerSearch;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandSupportResponse extends BaseCommand<MiniPluginChat> {

    final MiniPluginPermission _miniPluginPermission;

    public CommandSupportResponse(final MiniPluginChat miniPluginChat,
                                  final MiniPluginPermission miniPluginPermission) {
        super(miniPluginChat, "supportresponse", "<Player> <Message>", "Respond to a help request.", Set.of("ma", "sr"),
                MiniPluginChat.PERM.COMMAND_SUPPORT_STAFF);
        _miniPluginPermission = miniPluginPermission;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) {
            Player target = _miniPlugin._hexusPlugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(F.fMain(this, "Could not find a player with specified name."));
                return;
            }

            final PermissionGroup permissionGroup = sender instanceof Player ?
                    PermissionGroup.getGroupWithHighestWeight(
                            _miniPluginPermission._permissionProfiles.get((Player) sender)._groups()) : null;

            for (Player player : _miniPlugin._hexusPlugin.getServer().getOnlinePlayers()) {
                if (player.equals(sender) || player.equals(target) ||
                        player.hasPermission(PermissionGroup.TRAINEE.name())) {
                    final PermissionGroup targetGroup = PermissionGroup.getGroupWithHighestWeight(
                            _miniPluginPermission._permissionProfiles.get(player)._groups());

                    final String sourceStr = F.fPermissionGroup(permissionGroup) + " " + sender.getName();
                    final String targetStr = F.fPermissionGroup(targetGroup) + " " + player.getName();

                    player.sendMessage(
                            sourceStr + C.fReset + C.cPurple + " -> " + C.fReset + targetStr + C.fReset + " " +
                                    C.cPurple + String.join(" ", Arrays.stream(args).toList().subList(1, args.length)));
                    if (player.equals(target)) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                    }
                }
            }

            return;
        }

        sender.sendMessage(help(alias));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 1) return List.of();
        return PlayerSearch.onlinePlayerCompletions(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), sender,
                false);
    }

}
