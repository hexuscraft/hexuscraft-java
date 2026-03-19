package net.hexuscraft.core.punish;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.database.messages.PunishPunishmentAppliedMessage;
import net.hexuscraft.common.database.queries.PunishQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.enums.PunishType;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.punish.command.CommandPunish;
import net.hexuscraft.core.punish.command.CommandPunishHistory;
import net.hexuscraft.core.punish.command.CommandRules;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class CorePunish extends MiniPlugin<HexusPlugin> {

    public enum PERM implements IPermission {
        COMMAND_PUNISH,
        COMMAND_PUNISH_HISTORY,
        COMMAND_RULES
    }

    private static final Logger log = LoggerFactory.getLogger(CorePunish.class);
    private CoreCommand _pluginCommand;
    private CoreDatabase _coreDatabase;
    private CorePortal _corePortal;

    public CorePunish(final HexusPlugin plugin) {
        super(plugin,
                "Punish");
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_PUNISH_HISTORY);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_RULES);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandRules(this));
        _pluginCommand.register(new CommandPunish(this));
        _pluginCommand.register(new CommandPunishHistory(this));

        _coreDatabase._database.registerConsumer(PunishPunishmentAppliedMessage.CHANNEL_NAME,
                (_, _, message) -> {
                    final PunishPunishmentAppliedMessage punishPunishmentAppliedMessage = PunishPunishmentAppliedMessage.parse(
                            message);
                    _hexusPlugin.runAsync(() -> {
                        final Map<String, String> rawData = new HashMap<>(_coreDatabase._database._jedis.hgetAll(
                                PunishQueries.PUNISHMENT(punishPunishmentAppliedMessage._punishmentUUID)));
                        rawData.put("id",
                                punishPunishmentAppliedMessage._punishmentUUID.toString());

                        final PunishData punishData = new PunishData(rawData);

                        final OfflinePlayer targetOfflinePlayer;
                        try {
                            targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(
                                    punishPunishmentAppliedMessage._targetUUID);
                        } catch (final IOException ex) {
                            logWarning(
                                    "Could not fetch offline player for punish target '" + punishPunishmentAppliedMessage._targetUUID + "': " + ex.getMessage());
                            return;
                        }
                        if (targetOfflinePlayer == null) return;

                        final AtomicReference<String> staffName = new AtomicReference<>();
                        if (punishData.staffId.equals(UtilUniqueId.EMPTY_UUID)) staffName.set(_hexusPlugin.getServer()
                                .getConsoleSender()
                                .getName());
                        else try {
                            staffName.set(Objects.requireNonNull(PlayerSearch.offlinePlayerSearch(punishData.staffId))
                                    .getName());
                        } catch (final IOException | NullPointerException ex) {
                            logSevere(ex);
                            staffName.set("<unknown>");
                        }

                        Optional.ofNullable(targetOfflinePlayer.getPlayer())
                                .ifPresent((final Player targetPlayer) -> {
                                    final String punishMessage = F.fPunish(punishData);
                                    targetPlayer.sendMessage(punishMessage);
                                    targetPlayer.playSound(targetPlayer.getLocation(),
                                            Sound.CAT_MEOW,
                                            Float.MAX_VALUE,
                                            0.6F);
                                });

                        _hexusPlugin.getServer()
                                .getOnlinePlayers()
                                .stream()
                                .filter((final Player onlineStaffPlayer) -> onlineStaffPlayer.hasPermission(
                                        PermissionGroup.TRAINEE.name()))
                                .forEach((final Player onlineStaffPlayer) -> {
                                    switch (punishData.type) {
                                        case PunishType.WARNING -> onlineStaffPlayer.sendMessage(F.fMain(this,
                                                F.fItem(staffName.get()),
                                                " warned ",
                                                F.fItem(targetOfflinePlayer.getName()),
                                                ".\n",
                                                F.fMain("",
                                                        "Reason: ",
                                                        C.cWhite + punishData.reason)));
                                        case PunishType.MUTE -> onlineStaffPlayer.sendMessage(F.fMain(this,
                                                F.fItem(staffName.get()),
                                                " muted ",
                                                F.fItem(targetOfflinePlayer.getName()),
                                                ".\n",
                                                F.fMain("",
                                                        "Duration: ",
                                                        C.cWhite + F.fTime(punishData.length)),
                                                "\n",
                                                F.fMain("",
                                                        "Reason: ",
                                                        C.cWhite + punishData.reason)));
                                        case PunishType.BAN -> onlineStaffPlayer.sendMessage(F.fMain(this,
                                                F.fItem(staffName.get()),
                                                " banned ",
                                                F.fItem(targetOfflinePlayer.getName()),
                                                ".\n",
                                                F.fMain("",
                                                        "Duration: ",
                                                        C.cWhite + F.fTime(punishData.length)),
                                                "\n",
                                                F.fMain("",
                                                        "Reason: ",
                                                        C.cWhite + punishData.reason)));
                                        default -> logWarning("Unknown punishment type: " + punishData.type);
                                    }
                                });
                    });
                });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onConnect(final AsyncPlayerPreLoginEvent event) {
        if (!event.getLoginResult()
                .equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) return;

        try {
            final UnifiedJedis jedis = _coreDatabase._database._jedis;
            final Set<UUID> punishmentIds = jedis.smembers(PunishQueries.LIST(event.getUniqueId()))
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());

            // We want to display the longest ban remaining.
            // If there are multiple bans with the same remaining time (usually multiple perm bans), display the most recent ban.
            // If there are multiple bans matching this and were also applied at the EXACT same time (??), fate decides the displayed message.

            final Set<PunishData> activePunishments = new HashSet<>();

            for (final UUID punishmentUniqueId : punishmentIds) {
                try {
                    final Map<String, String> rawData = new HashMap<>(
                            jedis.hgetAll(PunishQueries.PUNISHMENT(punishmentUniqueId)));
                    rawData.put("id",
                            punishmentUniqueId.toString());

                    final PunishData punishData = new PunishData(rawData);
                    if (!punishData.active) continue;
                    if (!punishData.type.equals(PunishType.BAN)) continue;

                    if (punishData.length == -1) { // permanent ban
                        activePunishments.add(punishData);
                        continue;
                    }

                    final long remaining = punishData.getRemaining();
                    if (remaining <= 0) {
                        _coreDatabase._database._jedis.hset(PunishQueries.PUNISHMENT(punishmentUniqueId),
                                Map.of("active",
                                        "false",
                                        "removeOrigin",
                                        Long.toString(System.currentTimeMillis()),
                                        "removeReason",
                                        "EXPIRED",
                                        "removeServer",
                                        _corePortal._serverName,
                                        "removeStaffId",
                                        UtilUniqueId.EMPTY_UUID.toString(),
                                        "removeStaffServer",
                                        _corePortal._serverName));
                        continue;
                    }

                    activePunishments.add(punishData);
                } catch (final JedisException ex) {
                    logWarning("Error while checking punish punish player");
                }
            }

            if (activePunishments.isEmpty()) return;

            final AtomicReference<PunishData> punishData = new AtomicReference<>();
            if (activePunishments.size() > 1) {
                for (PunishData data : activePunishments) {
                    if (punishData.get() == null) {
                        punishData.set(data);
                        continue;
                    }
                    punishData.set(punishData.get()
                            .compare(data));
                }
            } else {
                punishData.set(activePunishments.iterator()
                        .next());
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    F.fPunish(punishData.get()));
        } catch (final JedisException ex) {
            logWarning("Error while fetching punishment punish for '" + event.getName() + "': " + ex.getMessage());
        }
    }

    public BukkitTask punishAsync(final UUID targetUUID, final UUID staffUUID, final PunishType punishType, final long lengthMillis, final String reason) {
        return _hexusPlugin.runAsync(() -> new PunishData(Map.ofEntries(Map.entry("id",
                        UUID.randomUUID()
                                .toString()),
                Map.entry("type",
                        punishType.name()),
                Map.entry("active",
                        "true"),
                Map.entry("origin",
                        Long.toString(System.currentTimeMillis())),
                Map.entry("length",
                        Long.toString(lengthMillis)),
                Map.entry("reason",
                        reason),
                Map.entry("server",
                        _corePortal._serverName),
                Map.entry("staffId",
                        (staffUUID == null ? UtilUniqueId.EMPTY_UUID : staffUUID).toString()),
                Map.entry("staffServer",
                        _corePortal._serverName))).publish(_coreDatabase._database._jedis,
                targetUUID));
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (!inventory.getName()
                .contains("Punish ")) return;

        event.setCancelled(true);

        if (!event.getClick()
                .isLeftClick()) return;

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

        final List<String> skullLore = skull.getItemMeta()
                .getLore();

        final UUID uuid = UUID.fromString(ChatColor.stripColor(skullLore.getFirst()));
        final OfflinePlayer targetOfflinePlayer;
        try {
            targetOfflinePlayer = PlayerSearch.offlinePlayerSearch(uuid,
                    clicker);
            if (targetOfflinePlayer == null) return;
        } catch (final IOException ex) {
            logSevere(ex);
            clicker.sendMessage(F.fMain(this,
                    F.fError(
                            "IOException while fetching OfflinePlayer. Please try again later or contact dev-ops if this issue persists.")));
            return;
        }

        punishAsync(targetOfflinePlayer.getUniqueId(),
                clicker.getUniqueId(),
                punishType,
                punishLengthMillis,
                ChatColor.stripColor(skullLore.get(2)));
        clicker.closeInventory();
    }

}