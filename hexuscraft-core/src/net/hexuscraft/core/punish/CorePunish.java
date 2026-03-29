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
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.punish.command.CommandPunish;
import net.hexuscraft.core.punish.command.CommandPunishHistory;
import net.hexuscraft.core.punish.command.CommandRules;
import org.bukkit.*;
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

    private static final Logger log = LoggerFactory.getLogger(CorePunish.class);
    private final long ONE_DAY_MILLIS = 86400000;
    private CoreCommand _pluginCommand;
    private CoreDatabase _coreDatabase;
    private CorePortal _corePortal;

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

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_PUNISH_HISTORY);
        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_RULES);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH);
        PermissionGroup.TRAINEE._permissions.add(PERM.PUNISH_ALERTS);
        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_1);

        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_2);
        PermissionGroup.MODERATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_3);

        PermissionGroup.ADMINISTRATOR._permissions.add(PERM.COMMAND_PUNISH_SEVERITY_4);
    }

    @Override
    public void onEnable()
    {
        _pluginCommand.register(new CommandRules(this));
        _pluginCommand.register(new CommandPunish(this));
        _pluginCommand.register(new CommandPunishHistory(this));

        _coreDatabase._database.registerConsumer(PunishPunishmentAppliedMessage.CHANNEL_NAME, (_, _, rawMessage) ->
        {
            PunishData punishData = PunishPunishmentAppliedMessage.fromString(rawMessage)._punishData;

            _hexusPlugin.runAsync(() ->
            {
                OfflinePlayer target;
                try
                {
                    target = PlayerSearch.offlinePlayerSearch(punishData._targetUUID);
                    assert (target != null);
                }
                catch (IOException | AssertionError ex)
                {
                    logWarning("Could not fetch offline player for punish target '" +
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
                    catch (IOException | NullPointerException ex)
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
    private void onConnect(AsyncPlayerPreLoginEvent event)
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
            // decides the displayed message.

            Set<PunishData> activePunishments = new HashSet<>();

            for (UUID punishmentUniqueId : punishmentIds)
            {
                try
                {
                    Map<String, String> rawData = new HashMap<>(jedis.hgetAll(PunishQueries.PUNISHMENT(
                            punishmentUniqueId)));
                    rawData.put("id", punishmentUniqueId.toString());

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
        PunishData punishData = new PunishData(Map.ofEntries(Map.entry("id", UUID.randomUUID().toString()),
                Map.entry("type", punishType.name()),
                Map.entry("active", "true"),
                Map.entry("origin", Long.toString(System.currentTimeMillis())),
                Map.entry("length", Long.toString(lengthMillis)),
                Map.entry("reason", reason),
                Map.entry("server", _corePortal._serverName),
                Map.entry("staffId", (staffUUID == null ? UtilUniqueId.EMPTY_UUID : staffUUID).toString()),
                Map.entry("staffServer", _corePortal._serverName)));
        return _hexusPlugin.runAsync(() ->
        {
            punishData.publish(_coreDatabase._database._jedis);
            if (callback != null)
            {
                callback.accept(punishData);
            }
        });
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player staff))
        {
            return;
        }

        Inventory inventory = event.getInventory();
        String inventoryName = inventory.getName();

        if (inventory.getName().contains("Punish "))
        {
            event.setCancelled(true);

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null)
            {
                return;
            }
            if (!currentItem.hasItemMeta())
            {
                return;
            }

            ItemMeta itemMeta = currentItem.getItemMeta();
            if (!itemMeta.hasDisplayName())
            {
                return;
            }

            List<String> skullLore = inventory.getItem(4).getItemMeta().getLore();

            OfflinePlayer target;
            try
            {
                target = PlayerSearch.offlinePlayerSearch(UUID.fromString(ChatColor.stripColor(skullLore.getFirst())),
                        staff);
                assert (target != null);
            }
            catch (IOException | AssertionError ex)
            {
                logSevere(ex);
                staff.sendMessage(F.fMain(this,
                        F.fError("There was an error while fetching your punishment target. Please try again later or" +
                                " " +
                                "contact an administrator if this issue persists.")));
                return;
            }

            AtomicReference<PunishType> punishType = new AtomicReference<>();
            AtomicLong punishLengthMillis = new AtomicLong(-1);
            switch (ChatColor.stripColor(itemMeta.getDisplayName()))
            {
                case "Punishment History" ->
                {
                    openHistoryGui(staff, target);
                    return;
                }
                case "Friendly Warning" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.WARNING);
                }
                case "Chat Severity 1" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.MUTE);
                    punishLengthMillis.set(ONE_DAY_MILLIS);
                }
                case "Gameplay Severity 1" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS);
                }
                case "Client Severity 1" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 7);
                }
                case "Chat Severity 2" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.MUTE);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 3);
                }
                case "Gameplay Severity 2" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 3);
                }
                case "Client Severity 2" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 14);
                }
                case "Chat Severity 3" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.MUTE);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 5);
                }
                case "Gameplay Severity 3" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 5);
                }
                case "Client Severity 3" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                    punishLengthMillis.set(ONE_DAY_MILLIS * 28);
                }
                case "Permanent Mute" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.MUTE);
                }
                case "Permanent Ban" ->
                {
                    if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
                    {
                        return;
                    }
                    punishType.set(PunishType.BAN);
                }
            }

            if (punishType.get() == null)
            {
                return;
            }

            punishAsync(target.getUniqueId(),
                    staff.getUniqueId(),
                    punishType.get(),
                    punishLengthMillis.get(),
                    ChatColor.stripColor(skullLore.get(2)));
            staff.closeInventory();
        }
        else if (inventoryName.contains("Punishment History "))
        {
            event.setCancelled(true);

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null)
            {
                return;
            }
            if (!currentItem.hasItemMeta())
            {
                return;
            }

            ItemMeta itemMeta = currentItem.getItemMeta();
            if (!itemMeta.hasDisplayName())
            {
                return;
            }

            List<String> skullLore = inventory.getItem(4).getItemMeta().getLore();

            OfflinePlayer target;
            try
            {
                target = PlayerSearch.offlinePlayerSearch(UUID.fromString(ChatColor.stripColor(skullLore.getFirst())),
                        staff);
                assert (target != null);
            }
            catch (IOException | AssertionError ex)
            {
                logSevere(ex);
                staff.sendMessage(F.fMain(this,
                        F.fError("There was an error while fetching your punishment target. Please try again later or" +
                                " " +
                                "contact an administrator if this issue persists.")));
            }
        }
    }

    public void openPunishGui(Player staff, OfflinePlayer target, String reason)
    {
        Inventory inventory = _hexusPlugin.getServer().createInventory(staff, 6 * 9, "Punish - " + target.getName());

        ItemStack targetSkull = UtilItem.createItemSkull(target.getName(),
                C.cGreen + C.fBold + target.getName(),
                target.getUniqueId().toString(),
                "",
                C.cWhite + reason);

        ItemStack viewHistory = UtilItem.createItem(Material.NAME_TAG,
                C.cBlue + C.fBold + "Punishment History",
                "View the punishment history of " + F.fItem(target.getName()));

        ItemStack chatHeader = UtilItem.createItem(Material.BOOK_AND_QUILL, C.cBlue + C.fBold + "Chat Offenses");
        ItemStack chat1 = UtilItem.createItemWool(DyeColor.LIME,
                C.cGreen + C.fBold + "Chat Severity 1",
                "1 Day Mute",
                "",
                "Light chat offense");
        ItemStack chat2 = UtilItem.createItemWool(DyeColor.YELLOW,
                C.cYellow + C.fBold + "Chat Severity 2",
                "3 Days Mute",
                "",
                "Moderate chat offense");
        ItemStack chat3 = UtilItem.createItemWool(DyeColor.ORANGE,
                C.cGold + C.fBold + "Chat Severity 3",
                "5 Days Mute",
                "",
                "Heavy chat offense");

        ItemStack gameplayHeader = UtilItem.createItem(Material.IRON_BLOCK, C.cBlue + C.fBold + "Gameplay Offenses");
        ItemStack gameplay1 = UtilItem.createItemWool(DyeColor.LIME,
                C.cGreen + C.fBold + "Gameplay Severity 1",
                "1 Day Ban",
                "",
                "Light gameplay offense");
        ItemStack gameplay2 = UtilItem.createItemWool(DyeColor.YELLOW,
                C.cYellow + C.fBold + "Gameplay Severity 2",
                "3 Days Ban",
                "",
                "Moderate gameplay offense");
        ItemStack gameplay3 = UtilItem.createItemWool(DyeColor.ORANGE,
                C.cGold + C.fBold + "Gameplay Severity 3",
                "5 Days Ban",
                "",
                "Heavy gameplay offense");

        ItemStack clientHeader = UtilItem.createItem(Material.IRON_SWORD, C.cBlue + C.fBold + "Client Offenses");
        ItemStack client1 = UtilItem.createItemWool(DyeColor.LIME,
                C.cGreen + C.fBold + "Client Severity 1",
                "7 Days Ban",
                "",
                "Light client offense");
        ItemStack client2 = UtilItem.createItemWool(DyeColor.YELLOW,
                C.cYellow + C.fBold + "Client Severity 2",
                "14 Days Ban",
                "",
                "Moderate client offense");
        ItemStack client3 = UtilItem.createItemWool(DyeColor.ORANGE,
                C.cGold + C.fBold + "Client Severity 3",
                "28 Days Ban",
                "",
                "Heavy client offense");

        ItemStack miscHeader = UtilItem.createItem(Material.LEVER, C.cBlue + C.fBold + "Miscellaneous");
        ItemStack miscWarn = UtilItem.createItem(Material.PAPER,
                C.cGreen + C.fBold + "Friendly Warning",
                "Inform someone that they are breaking the rules");
        ItemStack miscMute = UtilItem.createItem(Material.BOOK,
                C.cRed + C.fBold + "Permanent Mute",
                "Severity 4",
                "",
                "Severe chat offense");
        ItemStack miscBan = UtilItem.createItem(Material.REDSTONE_BLOCK,
                C.cRed + C.fBold + "Permanent Ban",
                "Severity 4",
                "",
                "Severe gameplay/client offense");

        inventory.setItem(4, targetSkull);
        inventory.setItem(10, chatHeader);
        inventory.setItem(12, gameplayHeader);
        inventory.setItem(14, clientHeader);
        inventory.setItem(16, miscHeader);

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name()))
        {
            inventory.setItem(19, chat1);
            inventory.setItem(21, gameplay1);
            inventory.setItem(23, client1);
            inventory.setItem(25, miscWarn);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name()))
        {
            inventory.setItem(28, chat2);
            inventory.setItem(30, gameplay2);
            inventory.setItem(32, client2);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name()))
        {
            inventory.setItem(37, chat3);
            inventory.setItem(39, gameplay3);
            inventory.setItem(41, client3);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name()))
        {
            inventory.setItem(34, miscMute);
            inventory.setItem(43, miscBan);
        }

        if (staff.hasPermission(PERM.COMMAND_PUNISH_HISTORY.name()))
        {
            inventory.setItem(53, viewHistory);
        }

        staff.openInventory(inventory);
    }

    public void openHistoryGui(Player viewer, OfflinePlayer target)
    {
        boolean viewerCanPunish = viewer.hasPermission(PERM.COMMAND_PUNISH.name());

        Inventory gui = _hexusPlugin.getServer().createInventory(viewer, 6 * 9, "Punish History - " + target.getName());

        ItemStack targetSkull = UtilItem.createItemSkull(target.getName(),
                C.cGreen + C.fBold + target.getName(),
                target.getUniqueId().toString(),
                "",
                C.cWhite + "Viewing punishment history");

        //        ItemStack openPunishGui = UtilItem.createItem(Material.NAME_TAG, C.cBlue + C.fBold + "Apply
        //        Punishment",
        //                "Open the punishment menu for " + F.fItem(target.getName()));
        //
        //        if (viewerCanPunish) gui.setItem(53, openPunishGui);

        gui.setItem(4, targetSkull);


        viewer.openInventory(gui);
    }

}