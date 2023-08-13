package net.hexuscraft.hub.player.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.hub.Hub;
import net.hexuscraft.hub.player.PluginPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandSpawn extends BaseCommand {

    public CommandSpawn(PluginPlayer pluginPlayer) {
        super(pluginPlayer, "spawn", "[Player]", "Warp to spawn.", Set.of("stuck", "hub"), PluginPlayer.PERM.COMMAND_SPAWN);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            sender.sendMessage(help(alias));
            return;
        }

        Player target;

        if (args.length == 1) {
            if (!sender.hasPermission(PermissionGroup.TRAINEE.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            Player[] players = PlayerSearch.onlinePlayerSearch(_miniPlugin._javaPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (players.length != 1) {
                return;
            }
            target = players[0];
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(F.fMain(this) + "Only players can teleport themselves.");
            return;
        } else {
            target = (Player) sender;
        }

        target.teleport(((Hub) _miniPlugin._javaPlugin).getSpawn());
        if (target.getName().equals(sender.getName())) {
            target.sendMessage(F.fMain(this) + "You teleported to spawn.");
            return;
        }

        target.sendMessage(F.fMain(this) + "You were teleported to spawn by " + F.fEntity(sender) + ".");
        sender.sendMessage(F.fMain(this) + "Teleported " + F.fEntity(target) + " to spawn.");
        _miniPlugin._javaPlugin.getServer().getOnlinePlayers().forEach(staff -> {
            if (!staff.hasPermission(PermissionGroup.TRAINEE.name())) {
                return;
            }
            staff.sendMessage(F.fStaff() + F.fMain(this) + F.fEntity(sender) + " teleported " + F.fEntity(target) + " to spawn.");
        });
    }
}
