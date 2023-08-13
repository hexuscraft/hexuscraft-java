package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandServer extends BaseCommand {

    public CommandServer(PluginPortal pluginPortal) {
        super(pluginPortal, "server", "[Name]", "Teleport to a server.", Set.of("sv", "portal"), PluginPortal.PERM.COMMAND_SERVER);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(F.fMain(this) + "Only players can teleport to a server.");
                return;
            }
            String server = args[0];
            if (server.matches("^(Staff-)\\d+$") && !sender.hasPermission(PermissionGroup.TRAINEE.name())) {
                sender.sendMessage(F.fPermissionGroup(PermissionGroup.TRAINEE));
                return;
            }
            if (server.matches("^(Build-)\\d+$") && !sender.hasPermission(PermissionGroup.BUILDER.name())) {
                sender.sendMessage(F.fPermissionGroup(PermissionGroup.BUILDER));
                return;
            }
            ((PluginPortal) _miniPlugin).teleport(sender.getName(), server);
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(F.fMain(this) + "You are connected to " + F.fItem(((PluginPortal) _miniPlugin)._serverName) + ".");
            return;
        }
        sender.sendMessage(help(alias));
    }
}
