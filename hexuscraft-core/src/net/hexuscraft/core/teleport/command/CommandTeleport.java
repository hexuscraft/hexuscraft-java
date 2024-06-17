package net.hexuscraft.core.teleport.command;

import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.teleport.MiniPluginTeleport;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandTeleport extends BaseCommand<MiniPluginTeleport> {

    public CommandTeleport(final MiniPluginTeleport miniPluginTeleport) {
        super(miniPluginTeleport, "teleport", "[Players] (<Player> / <X> <Y> <Z> [<Yaw> <Pitch>])", "Teleport one or more players to a player or coordinates", Set.of("tp"), MiniPluginTeleport.PERM.COMMAND_TELEPORT);
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        final Player[] targets;
        final Location destination;
        final String destinationName;

        if (args.length == 1) { // Teleport self to a player
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport themselves")));
                return;
            }
            targets = new Player[]{player};

            final Player[] potentialDestinations = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (potentialDestinations.length != 1) return;
            destination = potentialDestinations[0].getLocation();
            destinationName = F.fItem(potentialDestinations[0].getDisplayName());
        } else if (args.length == 2) { // Teleport targets to a player
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_OTHERS.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length == 0) return;

            final Player[] potentialDestinations = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[1], sender);
            if (potentialDestinations.length != 1) return;
            destination = potentialDestinations[0].getLocation();
            destinationName = F.fItem(potentialDestinations[0].getDisplayName());
        } else if (args.length == 3) { // Teleport self to coords with yaw 0 pitch 0
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_COORDINATES.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport themselves")));
                return;
            }
            targets = new Player[]{player};

            final double x, y, z;
            try {
                x = Double.parseDouble(args[0]);
                y = Double.parseDouble(args[1]);
                z = Double.parseDouble(args[2]);
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid coordinates ", F.fList(new String[]{args[0], args[1], args[2]}), ".")));
                return;
            }
            destination = new Location(player.getWorld(), x, y, z, 0, 0);
            destinationName = F.fItem(destination);
        } else if (args.length == 4) { // Teleport targets to coords with yaw 0 pitch 0
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_COORDINATES.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_OTHERS.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length == 0) return;

            final World destinationWorld = sender instanceof Player player ? player.getWorld() : _miniPlugin._hexusPlugin.getServer().getWorlds().getFirst();
            if (destinationWorld == null) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid destination world")));
                return;
            }
            final double x, y, z;
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid coordinates ", F.fList(new String[]{args[1], args[2], args[3]}), ".")));
                return;
            }
            destination = new Location(destinationWorld, x, y, z, 0, 0);
            destinationName = F.fItem(destination);
        } else if (args.length == 5) { // Teleport self to coords with custom yaw-pitch
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_COORDINATES.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            if (!(sender instanceof final Player player)) {
                sender.sendMessage(F.fMain(this, F.fError("Only players can teleport themselves")));
                return;
            }
            targets = new Player[]{player};

            final double x, y, z;
            final float yaw, pitch;
            try {
                x = Double.parseDouble(args[0]);
                y = Double.parseDouble(args[1]);
                z = Double.parseDouble(args[2]);
                yaw = Float.parseFloat(args[3]);
                pitch = Float.parseFloat(args[4]);
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid coordinates ", F.fList(new String[]{args[0], args[1], args[2], F.fList(new String[]{args[3], args[4]})})), "."));
                return;
            }
            destination = new Location(player.getWorld(), x, y, z, yaw, pitch);
            destinationName = F.fItem(destination);
        } else if (args.length == 6) { // Teleport targets to coords with custom yaw-pitch
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_COORDINATES.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }
            if (!sender.hasPermission(MiniPluginTeleport.PERM.COMMAND_TELEPORT_OTHERS.name())) {
                sender.sendMessage(F.fInsufficientPermissions());
                return;
            }

            targets = PlayerSearch.onlinePlayerSearch(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers(), args[0], sender);
            if (targets.length == 0) return;

            final World destinationWorld = sender instanceof Player player ? player.getWorld() : _miniPlugin._hexusPlugin.getServer().getWorlds().getFirst();
            if (destinationWorld == null) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid destination world")));
                return;
            }
            final double x, y, z;
            final float yaw, pitch;
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
                yaw = Float.parseFloat(args[4]);
                pitch = Float.parseFloat(args[5]);
            } catch (final NumberFormatException ex) {
                sender.sendMessage(F.fMain(this, F.fError("Invalid coordinates ", F.fList(new String[]{args[1], args[2], args[3], F.fList(new String[]{args[4], args[5]})})), "."));
                return;
            }
            destination = new Location(destinationWorld, x, y, z, yaw, pitch);
            destinationName = F.fItem(destination);
        } else {
            sender.sendMessage(help(alias));
            return;
        }

        if (destination == null) {
            sender.sendMessage(F.fMain(this, F.fError("Invalid destination")));
            return;
        }

        sender.sendMessage(F.fMain(this, "Teleporting ", F.fList(Arrays.stream(targets).map(Player::getName).toList()), " to ", destinationName, "."));
        Arrays.stream(targets).forEach(target -> target.teleport(destination));
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 1) {
            final List<String> names = new ArrayList<>(List.of("*", "**"));
            if (sender instanceof final Player player) {
                names.add(".");
                names.addAll(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream().filter(target -> target.canSee(player)).map(Player::getName).toList());
                return names;
            }

            names.addAll(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
            return names;
        }

        if (args.length == 2) {
            final List<String> names = new ArrayList<>(List.of("*", "**"));
            if (sender instanceof final Player player) {
                names.add(".");
                names.add(Integer.toString(player.getLocation().getBlockZ()));
                names.addAll(_miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream().filter(target -> target.canSee(player)).map(Player::getName).toList());
                return names;
            }

            return _miniPlugin._hexusPlugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }

        if (args.length == 3 && sender instanceof final Player player)
            return List.of(Integer.toString(player.getLocation().getBlockY()));

        if (args.length == 4 && sender instanceof final Player player)
            return List.of(Integer.toString(player.getLocation().getBlockZ()));

        return List.of();
    }

}
