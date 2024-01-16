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

import java.util.HashSet;
import java.util.Set;

public class CommandSupport extends BaseCommand {

    private final Set<CommandSender> receivedTipSet;
    private final PluginPermission pluginPermission;

    public CommandSupport(final PluginChat pluginChat, final PluginPermission pluginPermission) {
        super(pluginChat, "support", "<Message>", "Request help from a staff member.", Set.of("a", "helpop"), PluginChat.PERM.COMMAND_SUPPORT);
        this.pluginPermission = pluginPermission;
        receivedTipSet = new HashSet<>();
    }

    @Override
    public final void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            final PermissionGroup permissionGroup = pluginPermission._primaryGroupMap.getOrDefault((Player) sender, PermissionGroup.MEMBER);

            if (!receivedTipSet.contains(sender)) {
                receivedTipSet.add(sender);
                sender.sendMessage(F.fMain(this) + "You should receive a reply shortly if a staff member is in your server. You can also report rule-breakers with " + F.fItem("/report") + ".");
            }

            for (final Player player : _miniPlugin._plugin.getServer().getOnlinePlayers()) {
                if (player.equals(sender) || player.hasPermission(PermissionGroup.TRAINEE.toString())) {
                    player.sendMessage(F.fPermissionGroup(permissionGroup) + " " + permissionGroup._color + sender.getName() + C.fReset + " " + C.cPurple + String.join(" ", args));
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, Integer.MAX_VALUE, 2);
                }
            }

            return;
        }

        sender.sendMessage(help(alias));
    }

}
