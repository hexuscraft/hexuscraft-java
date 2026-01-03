package net.hexuscraft.hub.player.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.hub.player.MiniPluginPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public final class CommandSpawn extends BaseCommand<MiniPluginPlayer> {

    public CommandSpawn(final MiniPluginPlayer miniPluginPlayer) {
        super(miniPluginPlayer, "spawn", "[Player]", "Warp to spawn.", Set.of("stuck", "hub", "lobby"),
                MiniPluginPlayer.PERM.COMMAND_SPAWN);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
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

            Player[] players =
                    PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0],
                            sender, matches -> matches.length != 1);
            if (players.length != 1) return;

            target = players[0];
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(F.fMain(this) + "Only players can teleport themselves.");
            return;
        } else {
            target = (Player) sender;
        }

        target.teleport(_miniPlugin._hexusPlugin._spawn);
        if (target.getName().equals(sender.getName())) {
            target.sendMessage(F.fMain(this) + "You teleported to spawn.");
            return;
        }

        target.sendMessage(F.fMain(this) + "You were teleported to spawn by " +
                F.fItem(sender instanceof final Player player ? player.getDisplayName() : sender.getName()) + ".");
        sender.sendMessage(F.fMain(this) + "Teleported " + F.fItem(target.getDisplayName()) + " to spawn.");
        _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().forEach(staff -> {
            if (!staff.hasPermission(PermissionGroup.TRAINEE.name())) return;
            staff.sendMessage(F.fStaff() + F.fMain(this,
                    F.fItem(sender instanceof final Player player ? player.getDisplayName() : sender.getName()),
                    " teleported ", F.fItem(target.getDisplayName()), " to spawn."));
        });
    }
}
