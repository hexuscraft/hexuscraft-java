package net.hexuscraft.core.portal.command;

import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.game.GameType;
import net.hexuscraft.core.portal.MiniPluginPortal;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class CommandNetworkGroupCreate extends BaseCommand<MiniPluginPortal> {

    private final String[] DISALLOWED_CHARACTERS = new String[]{":", "//", "\\\\", ".."};

    private final MiniPluginDatabase _miniPluginDatabase;

    CommandNetworkGroupCreate(final MiniPluginPortal miniPluginPortal, final MiniPluginDatabase miniPluginDatabase) {
        super(miniPluginPortal, "create",
                "<Name> <Required Permission> <Min Port #> <Max Port #> <Total Servers #> <Joinable Servers #> <Plugin File> <World Zip> <Ram #> <Capacity #> <World Edit TRUE/FALSE> <Server Timeout (ms) #> [Games]",
                "Create a server group.", Set.of("c", "add", "a"), MiniPluginPortal.PERM.COMMAND_NETWORK_GROUP_CREATE);
        _miniPluginDatabase = miniPluginDatabase;
    }

    @Override
    public void run(final CommandSender sender, final String alias, final String[] args) {
        if (args.length < 12) {
            sender.sendMessage(help(alias));
            return;
        }

        final String name;
        final PermissionGroup requiredPermission;
        final int minPort;
        final int maxPort;
        final int totalServers;
        final int joinableServers;
        final String plugin;
        final String worldZip;
        final int ram;
        final int capacity;
        final boolean worldEdit;
        final int timeoutMillis;
        final String[] games;

        try {
            name = args[0];
            if (name.length() > 100) throw new InvalidNetStatGroupCreateArgumentException(this, "Name",
                    "Length too long (must be between 1-100 characters)");
            if (name.isEmpty()) throw new InvalidNetStatGroupCreateArgumentException(this, "Name",
                    "Length too short (must be between 1-100 characters)");

            try {
                requiredPermission = PermissionGroup.valueOf(args[1]);
            } catch (final IllegalArgumentException ignored) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Required Permission",
                        "Invalid or unrecognised permission group");
            }

            try {
                minPort = Integer.parseInt(args[2]);
            } catch (final NumberFormatException ex) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Min Port #",
                        "Invalid or unrecognised integer");
            }
            if (minPort < 1) throw new InvalidNetStatGroupCreateArgumentException(this, "Min Port #",
                    "Port too small (must be between 1-65535)");
            if (minPort > 65535) throw new InvalidNetStatGroupCreateArgumentException(this, "Min Port #",
                    "Port too small (must be between 1-65535)");

            try {
                maxPort = Integer.parseInt(args[3]);
            } catch (final NumberFormatException ex) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Max Port #",
                        "Invalid or unrecognised integer");
            }
            if (maxPort < 1) throw new InvalidNetStatGroupCreateArgumentException(this, "Max Port #",
                    "Port too small (must be between 1-65535)");
            if (maxPort > 65535) throw new InvalidNetStatGroupCreateArgumentException(this, "Max Port #",
                    "Port too small (must be between 1-65535)");

            try {
                totalServers = Integer.parseInt(args[4]);
            } catch (final NumberFormatException ex) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Total Servers",
                        "Invalid or unrecognised integer");
            }
            if (totalServers < 0) throw new InvalidNetStatGroupCreateArgumentException(this, "Total Servers",
                    "Number too small (must be greater than 0)");

            try {
                joinableServers = Integer.parseInt(args[5]);
            } catch (final NumberFormatException ex) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Joinable Servers",
                        "Invalid or unrecognised integer");
            }
            if (joinableServers < 0) throw new InvalidNetStatGroupCreateArgumentException(this, "Joinable Servers",
                    "Number too small (must be greater than 0)");

            plugin = args[6];
            if (plugin.length() > 100) throw new InvalidNetStatGroupCreateArgumentException(this, "Plugin",
                    "Length too long (must be between 1-100 characters)");
            if (plugin.isEmpty()) throw new InvalidNetStatGroupCreateArgumentException(this, "Plugin",
                    "Length too short (must be between 1-100 characters)");
            for (final String characters : DISALLOWED_CHARACTERS) {
                if (!plugin.contains(characters)) continue;
                throw new InvalidNetStatGroupCreateArgumentException(this, "Plugin",
                        "Invalid characters '" + characters + "'");
            }

            worldZip = args[7];
            if (worldZip.length() > 100) throw new InvalidNetStatGroupCreateArgumentException(this, "World Zip",
                    "Length too long (must be between 1-100 characters)");
            if (worldZip.isEmpty()) throw new InvalidNetStatGroupCreateArgumentException(this, "World Zip",
                    "Length too short (must be between 1-100 characters)");
            for (final String characters : DISALLOWED_CHARACTERS) {
                if (!worldZip.contains(characters)) continue;
                throw new InvalidNetStatGroupCreateArgumentException(this, "World Zip",
                        "Invalid characters '" + characters + "'");
            }

            try {
                ram = Integer.parseInt(args[8]);
            } catch (final NumberFormatException ignored) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Ram", "Invalid or unrecognised integer");
            }
            if (ram < 1) throw new InvalidNetStatGroupCreateArgumentException(this, "Ram",
                    "Number too small (must be greater than 0)");

            try {
                capacity = Integer.parseInt(args[9]);
            } catch (final NumberFormatException ignored) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Capacity",
                        "Invalid or unrecognised integer");
            }
            if (capacity < 0) throw new InvalidNetStatGroupCreateArgumentException(this, "Capacity",
                    "Number too small (must be greater than or equal to 0)");

            try {
                worldEdit = Boolean.parseBoolean(args[10]);
            } catch (final Exception ignored) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "World Edit", "Must be TRUE or FALSE");
            }

            try {
                timeoutMillis = Integer.parseInt(args[11]);
            } catch (final NumberFormatException ignored) {
                throw new InvalidNetStatGroupCreateArgumentException(this, "Server Timeout",
                        "Invalid or unrecognised integer");
            }

            if (args.length == 12) games = new String[0];
            else games = Arrays.copyOfRange(args, 12, args.length);
            final List<String> availableGameTypeNames = Arrays.stream(GameType.values()).map(GameType::name).toList();
            for (final String gameName : games) {
                if (availableGameTypeNames.contains(gameName)) continue;
                throw new InvalidNetStatGroupCreateArgumentException(this, "Games",
                        "Invalid or unrecognised Game Type '" + gameName + "'");
            }

        } catch (final InvalidNetStatGroupCreateArgumentException ex) {
            sender.sendMessage(ex.constructMessage());
            return;
        }

        _miniPlugin._hexusPlugin.runAsync(() -> {
            try {
                new ServerGroupData(name, requiredPermission.name(), minPort, maxPort, totalServers, joinableServers,
                        plugin, worldZip, ram, capacity, worldEdit, timeoutMillis, games, null).update(
                        _miniPluginDatabase.getUnifiedJedis());
            } catch (final JedisException ex) {
                sender.sendMessage(F.fMain(this, F.fError(
                        "JedisException while updating server group data. Please contact dev-ops if this issue persists.")));
                _miniPlugin._hexusPlugin.logWarning(
                        "JedisException while '" + sender.getName() + "' creating server group '" + name + "': " +
                                ex.getMessage());
                return;
            }

            sender.sendMessage(F.fMain(this, F.fSuccess("Created server group with name ", F.fItem(name), ":\n"),
                    F.fItem("Name: " + F.fItem(name), "Required Permission: " + F.fItem(requiredPermission.name()),
                            "Min Port: " + F.fItem(Integer.toString(minPort)),
                            "Max Port: " + F.fItem(Integer.toString(maxPort)),
                            "Total Servers: " + F.fItem(Integer.toString(totalServers)),
                            "Joinable Servers: " + F.fItem(Integer.toString(joinableServers)),
                            "Plugin: " + F.fItem(plugin), "World Zip: " + F.fItem(worldZip),
                            "Ram: " + F.fItem(Integer.toString(ram)),
                            "Capacity: " + F.fItem(Integer.toString(capacity)),
                            "World Edit: " + F.fItem(Boolean.toString(worldEdit)),
                            "Server Timeout: " + F.fItem(Integer.toString(timeoutMillis)),
                            "Games: " + F.fItem(games))));
        });
    }

    @Override
    public List<String> tab(final CommandSender sender, final String alias, final String[] args) {
        if (args.length == 2) return Arrays.stream(PermissionGroup.values()).map(PermissionGroup::name).toList();
        return List.of();
    }

    private static final class InvalidNetStatGroupCreateArgumentException extends Exception {

        private final CommandNetworkGroupCreate _command;
        private final String _argument;
        private final String _reason;

        public InvalidNetStatGroupCreateArgumentException(final CommandNetworkGroupCreate command,
                                                          final String argument, final String reason) {
            _command = command;
            _argument = argument;
            _reason = reason;
        }

        private String constructMessage() {
            return F.fMain(_command, F.fError("Invalid argument ", F.fItem(_argument), ": "), F.fItem(_reason));
        }
    }
}
