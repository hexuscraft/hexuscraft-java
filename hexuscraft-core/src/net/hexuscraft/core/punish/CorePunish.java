package net.hexuscraft.core.punish;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.database.messages.PunishAppliedMessage;
import net.hexuscraft.common.database.queries.PunishQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.enums.PunishType;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.actionbar.ActionBar;
import net.hexuscraft.core.actionbar.CoreActionBar;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.punish.command.CommandPunish;
import net.hexuscraft.core.punish.command.CommandPunishHistory;
import net.hexuscraft.core.punish.command.CommandRules;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CorePunish extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_PUNISH,
        COMMAND_PUNISH_SEVERITY_1,
        COMMAND_PUNISH_SEVERITY_2,
        COMMAND_PUNISH_SEVERITY_3,
        COMMAND_PUNISH_SEVERITY_4,
        COMMAND_PUNISH_HISTORY,
        COMMAND_RULES,
        PUNISH_ALERTS,
    }

    static final Logger log = LoggerFactory.getLogger(CorePunish.class);
    final long ONE_DAY_MILLIS = 86400000;
    CoreCommand _pluginCommand;
    CoreDatabase _coreDatabase;
    CorePortal _corePortal;
    CoreActionBar _coreActionBar;
    Map<HumanEntity, PunishGui> _punishGuis;

    public CorePunish(HexusPlugin plugin)
    {
        super(plugin, "Punish");
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
        _coreActionBar = (CoreActionBar) dependencies.get(CoreActionBar.class);

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_PUNISH_HISTORY);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_RULES);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH);
        PermissionGroup.TRAINEE._permissions.add(PERM.PUNISH_ALERTS);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_1);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_2);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_3);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_4);

        _punishGuis = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        _pluginCommand.register(new CommandRules(this));
        _pluginCommand.register(new CommandPunish(this));
        _pluginCommand.register(new CommandPunishHistory(this));

        _coreDatabase._database.registerConsumer(PunishAppliedMessage.CHANNEL_NAME, (_, _, rawMessage) ->
        {
            PunishData punishData = PunishAppliedMessage.fromString(rawMessage)._punishData;

            _hexusPlugin.runAsync(() ->
            {
                OfflinePlayer target;
                try
                {
                    target = PlayerSearch.offlinePlayerSearch(punishData._targetUUID);
                    assert (target != null);
                }
                catch (AssertionError ex)
                {
                    logWarning("Could not fetch offline player for punish _target '" +
                            punishData._targetUUID +
                            "': " +
                            ex.getMessage());
                    return;
                }

                AtomicReference<String> staffName = new AtomicReference<>();
                if (punishData._staffUUID.equals(UtilUniqueId.EMPTY_UUID))
                {
                    staffName.set(_hexusPlugin.getServer().getConsoleSender().getName());
                }
                else
                {
                    try
                    {
                        staffName.set(Objects.requireNonNull(PlayerSearch.offlinePlayerSearch(punishData._staffUUID))
                                .getName());
                    }
                    catch (NullPointerException ex)
                    {
                        logSevere(ex);
                        return;
                    }
                }

                Optional.ofNullable(target.getPlayer()).ifPresent((Player targetPlayer) ->
                {
                    String punishMessage = F.fPunish(punishData);
                    targetPlayer.sendMessage(punishMessage);
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.CAT_MEOW, Float.MAX_VALUE, 0.6F);
                });

                _hexusPlugin.getServer()
                        .getOnlinePlayers()
                        .stream()
                        .filter((Player staff) -> staff.hasPermission(PERM.PUNISH_ALERTS.name()))
                        .forEach((Player staff) ->
                        {
                            switch (punishData._type)
                            {
                                case PunishType.WARNING -> staff.sendMessage(F.fStaff(this,
                                        F.fItem(staffName.get()),
                                        " warned ",
                                        F.fItem(target.getName()),
                                        ".\n",
                                        F.fStaff("", "Reason: ", C.cWhite + punishData._reason)));
                                case PunishType.KICK -> staff.sendMessage(F.fStaff(this,
                                        F.fItem(staffName.get()),
                                        " kicked ",
                                        F.fItem(target.getName()),
                                        ".\n",
                                        F.fStaff("", "Reason: ", C.cWhite + punishData._reason)));
                                case PunishType.MUTE -> staff.sendMessage(F.fStaff(this,
                                        F.fItem(staffName.get()),
                                        " muted ",
                                        F.fItem(target.getName()),
                                        ".\n",
                                        F.fStaff("", "Duration: ", C.cWhite + F.fTime(punishData._length)),
                                        "\n",
                                        F.fStaff("", "Reason: ", C.cWhite + punishData._reason)));
                                case PunishType.BAN -> staff.sendMessage(F.fStaff(this,
                                        F.fItem(staffName.get()),
                                        " banned ",
                                        F.fItem(target.getName()),
                                        ".\n",
                                        F.fStaff("", "Duration: ", C.cWhite + F.fTime(punishData._length)),
                                        "\n",
                                        F.fStaff("", "Reason: ", C.cWhite + punishData._reason)));
                            }
                            staff.playSound(staff.getLocation(), Sound.CAT_MEOW, Float.MAX_VALUE, 0.6F);
                        });
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onConnect(AsyncPlayerPreLoginEvent event)
    {
        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED))
        {
            return;
        }

        try
        {
            UnifiedJedis jedis = _coreDatabase._database._jedis;
            Set<UUID> punishmentIds = jedis.smembers(PunishQueries.RECEIVED(event.getUniqueId()))
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());

            // We want to display the longest ban remaining.
            // If there are multiple bans with the same remaining time (usually multiple perm bans), display the most
            // recent ban.
            // If there are multiple bans matching this and were also applied at the EXACT same time (??), fate
            // decides the displayed _message.

            Set<PunishData> activePunishments = new HashSet<>();

            for (UUID punishmentUniqueId : punishmentIds)
            {
                try
                {
                    Map<String, String> rawData =
                            new HashMap<>(jedis.hgetAll(PunishQueries.PUNISHMENT(punishmentUniqueId)));
                    rawData.put("uuid", punishmentUniqueId.toString());

                    PunishData punishData = new PunishData(rawData);
                    if (!punishData._active)
                    {
                        continue;
                    }
                    if (!punishData._type.equals(PunishType.BAN))
                    {
                        continue;
                    }

                    if (punishData._length == -1)
                    { // permanent ban
                        activePunishments.add(punishData);
                        continue;
                    }

                    long remaining = punishData.getRemaining();
                    if (remaining <= 0)
                    {
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
                }
                catch (JedisException ex)
                {
                    logWarning("Error while checking punish punish player");
                }
            }

            if (activePunishments.isEmpty())
            {
                return;
            }

            AtomicReference<PunishData> punishData = new AtomicReference<>();
            if (activePunishments.size() > 1)
            {
                for (PunishData data : activePunishments)
                {
                    if (punishData.get() == null)
                    {
                        punishData.set(data);
                        continue;
                    }
                    punishData.set(punishData.get().compare(data));
                }
            }
            else
            {
                punishData.set(activePunishments.iterator().next());
            }

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, F.fPunish(punishData.get()));
        }
        catch (JedisException ex)
        {
            logWarning("Error while fetching punishment punish for '" + event.getName() + "': " + ex.getMessage());
        }
    }

    public BukkitTask punishAsync(UUID targetUUID,
            UUID staffUUID,
            PunishType punishType,
            long lengthMillis,
            String reason)
    {
        return punishAsync(targetUUID, staffUUID, punishType, lengthMillis, reason, null);
    }

    public BukkitTask punishAsync(UUID targetUUID,
            UUID staffUUID,
            PunishType punishType,
            long lengthMillis,
            String reason,
            Consumer<PunishData> callback)
    {
        PunishData punishData = new PunishData(UUID.randomUUID(),
                punishType,
                true,
                System.currentTimeMillis(),
                lengthMillis,
                reason,
                targetUUID,
                _corePortal._serverName,
                staffUUID,
                _corePortal._serverName);

        return _hexusPlugin.runAsync(() ->
        {
            try
            {
                punishData.publish(_coreDatabase._database._jedis);
            }
            catch (JedisException ex)
            {
                logSevere(ex);
                callback.accept(null);
                return;
            }
            if (callback != null)
            {
                callback.accept(punishData);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void openPunishGui(Player staff, OfflinePlayer target, String reason)
    {
        Inventory inventory = _hexusPlugin.getServer().createInventory(staff, 6 * 9, "Punish - " + target.getName());

        ItemStack skull = UtilItem.createPlayerSkull(target.getName(),
                C.cGreen + C.fBold + target.getName(),
                target.getUniqueId().toString(),
                "",
                C.cWhite + reason,
                "",
                C.cYellow + C.fBold + "CLICK TO VIEW HISTORY");

        ItemStack mute1d = UtilItem.createWool(DyeColor.LIME,
                C.cGreen + C.fBold + "1 Day Mute",
                "Severity 1",
                "",
                "Light chat offense");
        ItemStack mute3d = UtilItem.createWool(DyeColor.LIME,
                C.cGreen + C.fBold + "3 Days Mute",
                "Severity 1",
                "",
                "Light chat offense");
        ItemStack mute5d = UtilItem.createWool(DyeColor.YELLOW,
                C.cYellow + C.fBold + "5 Days Mute",
                "Severity 2",
                "",
                "Moderate chat offense");
        ItemStack mute7d = UtilItem.createWool(DyeColor.YELLOW,
                C.cYellow + C.fBold + "7 Days Mute",
                "Severity 2",
                "",
                "Moderate chat offense");
        ItemStack mute14d = UtilItem.createWool(DyeColor.ORANGE,
                C.cGold + C.fBold + "14 Days Mute",
                "Severity 3",
                "",
                "Heavy chat offense");
        ItemStack mute28d = UtilItem.createWool(DyeColor.ORANGE,
                C.cGold + C.fBold + "28 Days Mute",
                "Severity 3",
                "",
                "Heavy chat offense");
        ItemStack mutePerm = UtilItem.create(Material.BOOK_AND_QUILL,
                C.cRed + C.fBold + "Permanent Mute",
                "Severity 4",
                "",
                "Severe chat offense");

        ItemStack ban1d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.LIME.getData(),
                C.cGreen + C.fBold + "1 Day Ban",
                "Severity 1",
                "",
                "Light gameplay offense");
        ItemStack ban3d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.LIME.getData(),
                C.cGreen + C.fBold + "3 Days Ban",
                "Severity 1",
                "",
                "Light gameplay offense");
        ItemStack ban5d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.YELLOW.getData(),
                C.cYellow + C.fBold + "5 Days Ban",
                "Severity 2",
                "",
                "Moderate gameplay offense");
        ItemStack ban7d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.YELLOW.getData(),
                C.cYellow + C.fBold + "7 Days Ban",
                "Severity 2",
                "",
                "Moderate gameplay offense");
        ItemStack ban14d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.ORANGE.getData(),
                C.cGold + C.fBold + "14 Days Ban",
                "Severity 3",
                "",
                "Heavy gameplay offense");
        ItemStack ban28d = UtilItem.createWithData(Material.HARD_CLAY,
                DyeColor.ORANGE.getData(),
                C.cGold + C.fBold + "28 Days Ban",
                "Severity 3",
                "",
                "Heavy gameplay offense");
        ItemStack banPerm = UtilItem.create(Material.REDSTONE_BLOCK,
                C.cRed + C.fBold + "Permanent Ban",
                "Severity 4",
                "",
                "Severe gameplay offense");

        ItemStack warning = UtilItem.create(Material.PAPER,
                C.cGreen + C.fBold + "Friendly Warning",
                "Inform someone that they are breaking the rules");

        inventory.setItem(4, skull);

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
        {
            inventory.setItem(19, mute1d);
            inventory.setItem(20, mute3d);
            inventory.setItem(22, ban1d);
            inventory.setItem(23, ban3d);
            inventory.setItem(25, warning);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
        {
            inventory.setItem(28, mute5d);
            inventory.setItem(29, mute7d);
            inventory.setItem(31, ban5d);
            inventory.setItem(32, ban7d);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
        {
            inventory.setItem(37, mute14d);
            inventory.setItem(38, mute28d);
            inventory.setItem(40, ban14d);
            inventory.setItem(41, ban28d);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
        {
            inventory.setItem(34, mutePerm);
            inventory.setItem(43, banPerm);
        }

        _punishGuis.put(staff,
                new PunishGui(inventory,
                        target,
                        reason,
                        skull,
                        warning,
                        mute1d,
                        mute3d,
                        mute5d,
                        mute7d,
                        mute14d,
                        mute28d,
                        mutePerm,
                        ban1d,
                        ban3d,
                        ban5d,
                        ban7d,
                        ban14d,
                        ban28d,
                        banPerm));
        staff.openInventory(inventory);
    }

    public void openHistoryGui(Player viewer, OfflinePlayer target)
    {
        boolean viewerCanPunish = viewer.hasPermission(PERM.COMMAND_PUNISH.name());

        Inventory gui = _hexusPlugin.getServer().createInventory(viewer, 6 * 9, "Punish History - " + target.getName());

        ItemStack targetSkull = UtilItem.createPlayerSkull(target.getName(),
                C.cGreen + C.fBold + target.getName(),
                target.getUniqueId().toString(),
                "",
                C.cWhite + "Viewing punishment history");

        //        ItemStack openPunishGui = UtilItem.createItem(Material.NAME_TAG, C.cBlue + C.fBold + "Apply
        //        Punishment",
        //                "Open the punishment menu for " + F.fItem(_target.getName()));
        //
        //        if (viewerCanPunish) gui.setItem(53, openPunishGui);

        gui.setItem(4, targetSkull);


        viewer.openInventory(gui);
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event)
    {
        _punishGuis.remove(event.getPlayer());
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player staff))
        {
            return;
        }

        PunishGui punishGui = _punishGuis.get(staff);
        if (punishGui != null && punishGui._inventory().equals(event.getInventory()))
        {
            event.setCancelled(true);

            AtomicReference<PunishType> type = new AtomicReference<>();
            AtomicLong lengthMillis = new AtomicLong(-1);

            if (event.getCurrentItem().equals(punishGui._skull()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_HISTORY.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                openHistoryGui(staff, punishGui._target());
            }
            else if (event.getCurrentItem().equals(punishGui._warning()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.WARNING);
            }
            else if (event.getCurrentItem().equals(punishGui._mute1d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS);
            }
            else if (event.getCurrentItem().equals(punishGui._ban1d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS);
            }
            else if (event.getCurrentItem().equals(punishGui._mute3d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS * 3);
            }
            else if (event.getCurrentItem().equals(punishGui._ban3d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS * 3);
            }
            else if (event.getCurrentItem().equals(punishGui._mute5d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS * 5);
            }
            else if (event.getCurrentItem().equals(punishGui._ban5d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS * 5);
            }
            else if (event.getCurrentItem().equals(punishGui._mute7d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS * 7);
            }
            else if (event.getCurrentItem().equals(punishGui._ban7d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS * 7);
            }
            else if (event.getCurrentItem().equals(punishGui._mute14d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS * 14);
            }
            else if (event.getCurrentItem().equals(punishGui._ban14d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS * 14);
            }
            else if (event.getCurrentItem().equals(punishGui._mute28d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
                lengthMillis.set(ONE_DAY_MILLIS * 28);
            }
            else if (event.getCurrentItem().equals(punishGui._ban28d()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
                lengthMillis.set(ONE_DAY_MILLIS * 28);
            }
            else if (event.getCurrentItem().equals(punishGui._mutePerm()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.MUTE);
            }
            else if (event.getCurrentItem().equals(punishGui._banPerm()))
            {
                if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
                {
                    staff.sendMessage(F.fInsufficientPermissions());
                    return;
                }
                type.set(PunishType.BAN);
            }

            if (type.get() == null)
            {
                return;
            }

            ActionBar actionBar = _coreActionBar.registerActionBar(new ActionBar(_coreActionBar,
                    staff,
                    1,
                    F.fActionBar(this,
                            "Processing your punishment against ",
                            F.fItem(punishGui._target().getName()),
                            "...")));

            staff.closeInventory();
            punishAsync(punishGui._target().getUniqueId(),
                    staff.getUniqueId(),
                    type.get(),
                    lengthMillis.get(),
                    punishGui._reason(),
                    (punishData ->
                    {
                        actionBar.setMessage(F.fActionBar(this,
                                F.fSuccess("Punishment successfully applied against ",
                                        F.fItem(punishGui._target().getName()),
                                        ".")));
                        _coreActionBar.unregisterActionBar(actionBar);
                        if (punishData != null)
                        {
                            return;
                        }
                        staff.sendMessage(F.fMain(this,
                                F.fError("There was an error while processing the punishment. Please try again " +
                                        "later or contact an administrator if this issue persists.")));
                    }));
        }
    }

}