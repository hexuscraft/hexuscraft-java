package net.hexuscraft.core.punish;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MessagedRunnable;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.core.punish.command.CommandPunish;
import net.hexuscraft.core.punish.command.CommandPunishHistory;
import net.hexuscraft.core.punish.command.CommandRules;
import net.hexuscraft.core.punish.data.PunishmentAppliedMessage;
import net.hexuscraft.database.queries.PunishQueries;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public final class MiniPluginPunish extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PUNISH, COMMAND_PUNISH_HISTORY, COMMAND_RULES
    }

    private MiniPluginCommand _pluginCommand;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPortal _miniPluginPortal;

    private final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public MiniPluginPunish(final HexusPlugin plugin) {
        super(plugin, "Punish");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_PUNISH_HISTORY);
        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_RULES);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandRules(this));
        _pluginCommand.register(new CommandPunish(this));
        _pluginCommand.register(new CommandPunishHistory(this));

        //noinspection rawtypes,unchecked
        _miniPluginDatabase.registerCallback(PunishmentAppliedMessage.CHANNEL_NAME, new MessagedRunnable(this) {

            @Override
            public void run() {
                final PunishmentAppliedMessage punishmentAppliedMessage = PunishmentAppliedMessage.fromJsonString(getMessage());
                _hexusPlugin.runAsync(() -> {
                    final Map<String, String> rawData = new HashMap<>(_miniPluginDatabase.getJedisPooled().hgetAll(PunishQueries.PUNISHMENT(punishmentAppliedMessage._punishmentId())));
                    rawData.put("id", punishmentAppliedMessage._punishmentId().toString());

                    final PunishData punishData = new PunishData(rawData);

                    final OfflinePlayer targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(punishmentAppliedMessage._targetUniqueId());
                    if (targetOfflinePlayer == null) return;

                    final OfflinePlayer staffOfflinePlayer = PlayerSearch.offlinePlayerSearch(punishData.staffUniqueId);
                    if (staffOfflinePlayer == null) return;

                    Optional.ofNullable(targetOfflinePlayer.getPlayer()).ifPresent((final Player targetPlayer) -> {
                        targetPlayer.sendMessage(F.fPunish(punishData));
                        targetPlayer.playSound(targetPlayer.getLocation(), Sound.CAT_MEOW, Integer.MAX_VALUE, 0.6F);

                        if (punishData.type.equals(PunishType.BAN)) targetPlayer.kickPlayer(F.fPunish(punishData));
                    });

                    _hexusPlugin.getServer().getOnlinePlayers().stream().filter((final Player onlineStaffPlayer) -> onlineStaffPlayer.hasPermission(PermissionGroup.TRAINEE.name())).forEach((final Player onlineStaffPlayer) -> {
                        switch (punishData.type) {
                            case PunishType.WARNING ->
                                    onlineStaffPlayer.sendMessage(F.fMain(this, F.fItem(staffOfflinePlayer.getName()), " warned ", F.fItem(targetOfflinePlayer.getName()), ".\n", F.fMain("", "Reason: ", C.cWhite + punishData.reason)));
                            case PunishType.MUTE ->
                                    onlineStaffPlayer.sendMessage(F.fMain(this, F.fItem(staffOfflinePlayer.getName()), " muted ", F.fItem(targetOfflinePlayer.getName()), ".\n", F.fMain("", "Duration: ", C.cWhite + F.fTime(punishData.length)), "\n", F.fMain("", "Reason: ", C.cWhite + punishData.reason)));
                            case PunishType.BAN ->
                                    onlineStaffPlayer.sendMessage(F.fMain(this, F.fItem(staffOfflinePlayer.getName()), " banned ", F.fItem(targetOfflinePlayer.getName()), ".\n", F.fMain("", "Duration: ", C.cWhite + F.fTime(punishData.length)), "\n", F.fMain("", "Reason: ", C.cWhite + punishData.reason)));
                            default -> logWarning("Unknown punishment type: " + punishData.type);
                        }
                    });
                });
            }
        });
    }

    @EventHandler
    private void onConnect(final AsyncPlayerPreLoginEvent event) {
        final Set<UUID> punishmentIds = _miniPluginDatabase.getJedisPooled().smembers(PunishQueries.LIST(event.getUniqueId())).stream().map(UUID::fromString).collect(Collectors.toSet());

        // We want to display the longest ban remaining.
        // If there are multiple bans with the same remaining time (usually multiple perm bans), display the most recent ban.
        // If there are multiple bans matching this and were also applied at the EXACT same time (??), fate decides the displayed message.

        final Set<PunishData> activePunishments = new HashSet<>();

        for (final UUID punishmentUniqueId : punishmentIds) {
            final Map<String, String> rawData = new HashMap<>(_miniPluginDatabase.getJedisPooled().hgetAll(PunishQueries.PUNISHMENT(punishmentUniqueId)));
            rawData.put("id", punishmentUniqueId.toString());

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
                _miniPluginDatabase.getJedisPooled().hset(PunishQueries.PUNISHMENT(punishmentUniqueId), Map.of("active", "false", "removeOrigin", Long.toString(System.currentTimeMillis()), "removeReason", "EXPIRED", "removeServer", _miniPluginPortal._serverName, "removeStaffId", DEFAULT_UUID.toString(), "removeStaffServer", _miniPluginPortal._serverName));
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

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, F.fPunish(punishData));
    }

    public void punish(final UUID targetUuid, final UUID staffUuid, final PunishType punishType, final long length, final String reason) {
        final PunishData punishData = new PunishData(Map.of("id", UUID.randomUUID().toString(), "type", punishType.name(), "active", "true", "origin", Long.toString(System.currentTimeMillis()), "length", Long.toString(length), "reason", reason, "server", _miniPluginPortal._serverName, "staffId", (staffUuid == null ? DEFAULT_UUID : staffUuid).toString(), "staffServer", _miniPluginPortal._serverName));

        _hexusPlugin.runAsync(() -> {
            _miniPluginDatabase.getJedisPooled().hset(PunishQueries.PUNISHMENT(punishData.uniqueId), punishData.toMap());
            _miniPluginDatabase.getJedisPooled().sadd(PunishQueries.LIST(targetUuid), punishData.uniqueId.toString());
            _miniPluginDatabase.getJedisPooled().publish(PunishmentAppliedMessage.CHANNEL_NAME, new PunishmentAppliedMessage(targetUuid, punishData.uniqueId).toJsonString());
        });
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
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

        final OfflinePlayer targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(UUID.fromString(ChatColor.stripColor(skull.getItemMeta().getLore().getFirst())));

        punish(targetOfflinePlayer.getUniqueId(), clicker.getUniqueId(), punishType, punishLengthMillis, ChatColor.stripColor(skull.getItemMeta().getLore().get(2)));
        clicker.closeInventory();
    }

}