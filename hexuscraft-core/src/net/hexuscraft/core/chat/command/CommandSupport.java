package net.hexuscraft.core.chat.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.chat.MiniPluginChat;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandSupport extends BaseCommand<MiniPluginChat> {

    private final MiniPluginPermission _miniPluginPermission;
    private final MiniPluginDatabase _miniPluginDatabase;

    public CommandSupport(final MiniPluginChat miniPluginChat, final MiniPluginPermission miniPluginPermission, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginChat, "support", "<Message>", "Request help from a staff member.", Set.of("a", "helpop"), MiniPluginChat.PERM.COMMAND_SUPPORT);
        _miniPluginPermission = miniPluginPermission;
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length > 0) {
            final PermissionGroup permissionGroup = _miniPluginPermission._primaryGroupMap.getOrDefault((Player) sender, PermissionGroup.MEMBER);

            if (!_miniPlugin._receivedTipSet.contains(sender)) {
                _miniPlugin._receivedTipSet.add(sender);
                sender.sendMessage(F.fMain(this, "You should receive a reply shortly if a staff member is available. You can also report rule-breakers with ", F.fItem("/report"), "."));
            }

            // TODO: Support tickets with pub/sub, claim command, respond command, close command, etc.
            for (final Player player : _miniPlugin._hexusPlugin.getServer().getOnlinePlayers()) {
                if (player.equals(sender) || player.hasPermission(PermissionGroup.TRAINEE.name())) {
                    player.sendMessage(F.fPermissionGroup(permissionGroup) + " " + permissionGroup._color + sender.getName() + C.fReset + " " + C.cPurple + String.join(" ", args));
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, Integer.MAX_VALUE, 2);
                }
            }


            return;
        }

        sender.sendMessage(help(alias));
    }

}
