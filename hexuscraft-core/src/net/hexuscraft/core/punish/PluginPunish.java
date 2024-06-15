package net.hexuscraft.core.punish;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.MojangSession;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.core.punish.command.CommandPunish;
import net.hexuscraft.core.punish.command.CommandPunishHistory;
import net.hexuscraft.core.punish.command.CommandRules;
import net.hexuscraft.database.queries.PunishQueries;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class PluginPunish extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PUNISH,
        COMMAND_PUNISH_HISTORY,
        COMMAND_RULES
    }

    private PluginCommand _pluginCommand;
    private PluginDatabase _pluginDatabase;
    private PluginPortal _pluginPortal;

    private final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public PluginPunish(final HexusPlugin plugin) {
        super(plugin, "Punish");
    }

    @Override
    public final void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
        _pluginPortal = (PluginPortal) dependencies.get(PluginPortal.class);

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PUNISH_HISTORY);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_RULES);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH);
    }

    @Override
    public final void onEnable() {
        _pluginCommand.register(new CommandRules(this));
        _pluginCommand.register(new CommandPunish(this));
        _pluginCommand.register(new CommandPunishHistory(this));

        _pluginDatabase.registerCallback("punishment", new MessagedRunnable(this) {

            @Override
            public void run() {
                final String[] data = getMessage().split(",");
                final String targetId = data[0];
                final String punishmentId = data[1];

                final Map<String, String> rawData = new HashMap<>(_pluginDatabase.getJedisPooled().hgetAll(PunishQueries.PUNISHMENT(punishmentId)));
                rawData.put("id", punishmentId);

                final PunishData punishData = new PunishData(rawData);

                final MojangSession targetSession;
                final MojangSession staffSession;
                try {
                    targetSession = PlayerSearch.fetchMojangSession(UUID.fromString(targetId));
                    staffSession = PlayerSearch.fetchMojangSession(punishData.staffId);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                final Player target = _miniPlugin._plugin.getServer().getPlayer(targetSession.name);

                if (punishData.type.equals(PunishType.WARNING)) {
                    if (target != null) {
                        target.sendMessage(F.fMain(this, "You received a warning."));
                        target.sendMessage(F.fMain("", "Reason: ", C.cWhite + punishData.reason));
                        target.playSound(target.getLocation(), Sound.CAT_MEOW, Integer.MAX_VALUE, 1);
                    }
                    _miniPlugin._plugin.getServer().getOnlinePlayers().forEach(staff -> {
                        if (!staff.hasPermission(PermissionGroup.TRAINEE.name())) return;
                        staff.sendMessage(F.fMain(this, F.fItem(staffSession.name), " warned ", F.fItem(targetSession.name), "."));
                        staff.sendMessage(F.fMain("", "Reason: ", C.cWhite + punishData.reason));
                    });
                    return;
                }
                if (punishData.type.equals(PunishType.MUTE)) {
                    if (target != null) {
                        target.sendMessage(F.fMain(this, "You were muted for ", F.fItem(F.fTime(punishData.length)), "."));
                        target.sendMessage(F.fMain("", "Reason: ", C.cWhite + punishData.reason));
                        target.playSound(target.getLocation(), Sound.CAT_MEOW, Integer.MAX_VALUE, 0.6F);
                    }
                    _miniPlugin._plugin.getServer().getOnlinePlayers().forEach(staff -> {
                        if (!staff.hasPermission(PermissionGroup.TRAINEE.name())) return;
                        staff.sendMessage(F.fMain(this, F.fItem(staffSession.name), " muted ", F.fItem(targetSession.name), " for ", F.fItem(F.fTime(punishData.length)), "."));
                        staff.sendMessage(F.fMain("", "Reason: ", C.cWhite + punishData.reason));
                    });
                    return;
                }
                if (punishData.type.equals(PunishType.BAN)) {
                    if (target != null) {
                        _miniPlugin._plugin.getServer().getScheduler().runTask(_miniPlugin._plugin, () -> target.kickPlayer(F.fPunishBan(UUID.fromString(punishmentId), punishData.reason, punishData.length)));
                    }
                    _miniPlugin._plugin.getServer().getOnlinePlayers().forEach(staff -> {
                        if (!staff.hasPermission(PermissionGroup.TRAINEE.name())) return;
                        staff.sendMessage(F.fMain(this, F.fItem(staffSession.name), " banned ", F.fItem(targetSession.name), " for ", F.fItem(F.fTime(punishData.length)), "."));
                        staff.sendMessage(F.fMain("", "Reason: ", C.cWhite + punishData.reason));
                    });
                    return;
                }
                throw new RuntimeException("Unknown punishment type: " + punishData);
            }
        });
    }

    @EventHandler
    final void onConnect(final AsyncPlayerPreLoginEvent event) {
        final Set<String> punishmentIds = _pluginDatabase.getJedisPooled().smembers(PunishQueries.LIST(event.getUniqueId().toString()));

        // We want to display the longest ban remaining.
        // If there are multiple bans with the same remaining time (usually multiple perm bans), display the most recent ban.
        // If there are multiple bans matching this and were also applied at the EXACT same time (??), fate decides the displayed message.

        final Set<PunishData> activePunishments = new HashSet<>();

        for (final String id : punishmentIds) {
            final Map<String, String> rawData = new HashMap<>(_pluginDatabase.getJedisPooled().hgetAll(PunishQueries.PUNISHMENT(id)));
            rawData.put("id", id);

            final PunishData punishData = new PunishData(rawData);
            if (!punishData.active) {
                continue;
            }
            if (!punishData.type.equals(PunishType.BAN)) {
                continue;
            }

            if (punishData.length == -1) { // permanent ban
                activePunishments.add(punishData);
                continue;
            }

            final long remaining = punishData.getRemaining();
            if (remaining <= 0) {
                _pluginDatabase.getJedisPooled().hset(PunishQueries.PUNISHMENT(id), Map.of(
                        "active", "false",
                        "removeOrigin", Long.toString(System.currentTimeMillis()),
                        "removeReason", "EXPIRED",
                        "removeServer", _pluginPortal._serverName,
                        "removeStaffId", DEFAULT_UUID.toString(),
                        "removeStaffServer", _pluginPortal._serverName
                ));
                continue;
            }

            activePunishments.add(punishData);
        }

        if (activePunishments.isEmpty()) {
            return;
        }

        //noinspection ReassignedVariable
        PunishData punishData = null;
        if (activePunishments.size() > 1) {
            for (PunishData data : activePunishments) {
                if (punishData == null) {
                    punishData = data;
                    continue;
                }
                punishData = punishData.compare(data);
            }
        } else {
            punishData = activePunishments.iterator().next();
        }

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, F.fPunishBan(punishData.id, punishData.reason, punishData.length == -1 ? -1 : punishData.getRemaining()));
    }

    final void punish(final UUID targetUuid, final UUID staffUuid, final PunishType punishType, final long length, final String reason) {
        PunishData punishData = new PunishData(Map.of(
                "id", UUID.randomUUID().toString(),
                "type", punishType.name(),
                "active", "true",
                "origin", Long.toString(System.currentTimeMillis()),
                "length", Long.toString(length),
                "reason", reason,
                "server", _pluginPortal._serverName,
                "staffId", staffUuid.toString(),
                "staffServer", _pluginPortal._serverName
        ));

        _pluginDatabase.getJedisPooled().hset(PunishQueries.PUNISHMENT(punishData.id.toString()), punishData.toMap());
        _pluginDatabase.getJedisPooled().sadd(PunishQueries.LIST(targetUuid.toString()), punishData.id.toString());
        _pluginDatabase.getJedisPooled().publish("punishment", targetUuid + "," + punishData.id);
    }

    @EventHandler
    final void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (!inventory.getName().contains("Punish ")) return;

        event.setCancelled(true);

        if (!event.getClick().isLeftClick()) return;

        final HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) return;

        final ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) return;
        if (!currentItem.hasItemMeta()) return;

        final ItemMeta itemMeta = currentItem.getItemMeta();
        if (!itemMeta.hasDisplayName()) return;

        final String displayName = itemMeta.getDisplayName();

        final PunishType punishType;
        final long punishLengthMillis;

        if (displayName.contains("Warning")) {
            if (!clicker.hasPermission(PermissionGroup.TRAINEE.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.WARNING;
            punishLengthMillis = -1;

        } else if (displayName.contains("1 Day Mute")) {
            if (!clicker.hasPermission(PermissionGroup.TRAINEE.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.MUTE;
            punishLengthMillis = (long) 24 * 60 * 60 * 1000;

        } else if (displayName.contains("3 Days Mute")) {
            if (!clicker.hasPermission(PermissionGroup.TRAINEE.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.MUTE;
            punishLengthMillis = (long) 3 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("5 Days Mute")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.MUTE;
            punishLengthMillis = (long) 5 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("Permanent Mute")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.MUTE;
            punishLengthMillis = -1;

        } else if (displayName.contains("1 Day Ban")) {
            if (!clicker.hasPermission(PermissionGroup.TRAINEE.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 24 * 60 * 60 * 1000;

        } else if (displayName.contains("3 Days Ban")) {
            if (!clicker.hasPermission(PermissionGroup.TRAINEE.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 3 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("5 Days Ban")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 5 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("7 Days Ban")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 7 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("14 Days Ban")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 14 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("30 Days Ban")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = (long) 30 * 24 * 60 * 60 * 1000;

        } else if (displayName.contains("Permanent Ban")) {
            if (!clicker.hasPermission(PermissionGroup.MODERATOR.name())) {
                clicker.sendMessage(F.fInsufficientPermissions());
                return;
            }
            punishType = PunishType.BAN;
            punishLengthMillis = -1;

        } else return;

        final ItemStack skull = inventory.getItem(4);
        final MojangSession targetSession;
        try {
            targetSession = PlayerSearch.fetchMojangSession(UUID.fromString(ChatColor.stripColor(skull.getItemMeta().getLore().getFirst())));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        punish(targetSession.uuid, clicker.getUniqueId(), punishType, punishLengthMillis, ChatColor.stripColor(skull.getItemMeta().getLore().get(2)));
        clicker.closeInventory();
    }

}