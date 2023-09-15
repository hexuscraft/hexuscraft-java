package net.hexuscraft.core.portal.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.portal.PluginPortal;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandServer extends BaseCommand {

    private final PluginPortal _portal;

    public CommandServer(PluginPortal pluginPortal) {
        super(pluginPortal, "server", "[Name]", "Teleport to a server.", Set.of("sv", "portal"), PluginPortal.PERM.COMMAND_SERVER);
        _portal = pluginPortal;
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(F.fMain(this) + "Only players can teleport to a server.");
                return;
            }
            String serverName = args[0];
            if (serverName.matches("^(Staff-)\\d+$") && !sender.hasPermission(PermissionGroup.TRAINEE.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }
            if (serverName.matches("^(Build-)\\d+$") && !sender.hasPermission(PermissionGroup.BUILDER.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }
            if (_portal._serverName.equals(serverName)) {
                sender.sendMessage(F.fMain(this) + "You are already connected to " + F.fItem(serverName) + ".");
                return;
            }
            if (!_portal.isServerActive(serverName)) {
                sender.sendMessage(F.fMain(this) + "Could not locate a server with name " + F.fItem(serverName) + ".");
                return;
            }
            _portal.teleport(sender.getName(), serverName);
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(F.fMain(this) + "You are connected to " + F.fItem(_portal._serverName) + ".");
            return;
        }
        sender.sendMessage(help(alias));
    }
}
