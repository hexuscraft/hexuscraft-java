package net.hexuscraft.core.report;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.ReportData;
import net.hexuscraft.common.database.messages.ReportSubmittedMessage;
import net.hexuscraft.common.database.queries.ReportQueries;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.enums.ReportSubmitReason;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.database.CoreDatabase;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.core.report.command.CommandReport;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CoreReport extends MiniPlugin<HexusPlugin>
{

    public enum PERM implements IPermission
    {
        COMMAND_REPORT,
        COMMAND_REPORT_HISTORY,
        REPORT_ALERTS
    }

    Map<HumanEntity, ReportGui> _reportGuis;
    CoreCommand _coreCommand;
    CoreDatabase _coreDatabase;
    CorePortal _corePortal;

    public CoreReport(HexusPlugin plugin)
    {
        super(plugin, "Reports");
        _reportGuis = new HashMap<>();

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_REPORT);

        PermissionGroup.TRAINEE._permissions.add(PERM.COMMAND_REPORT_HISTORY);
        PermissionGroup.TRAINEE._permissions.add(PERM.REPORT_ALERTS);
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable()
    {
        _coreCommand.register(new CommandReport(this));

        _coreDatabase._database.registerConsumer(ReportSubmittedMessage.CHANNEL_NAME, (_, _, rawMessage) ->
        {
            ReportSubmittedMessage parsedMessage = ReportSubmittedMessage.fromString(rawMessage);

            _hexusPlugin.runAsync(() ->
            {
                Map<String, String> rawData = new HashMap<>(_coreDatabase._database._jedis.hgetAll(ReportQueries.REPORT(
                        parsedMessage.reportUUID())));
                rawData.put("reportUUID", parsedMessage.reportUUID().toString());

                ReportData reportData = new ReportData(rawData);

                OfflinePlayer sender, target;

                try
                {
                    sender = PlayerSearch.offlinePlayerSearch(reportData.senderUUID);
                    assert (sender != null);
                }
                catch (IOException | AssertionError ex)
                {
                    logSevere(ex);
                    return;
                }

                try
                {
                    target = PlayerSearch.offlinePlayerSearch(reportData.targetUUID);
                    assert (target != null);
                }
                catch (IOException | AssertionError ex)
                {
                    logSevere(ex);
                    return;
                }

                _hexusPlugin.getServer()
                        .getOnlinePlayers()
                        .stream()
                        .filter(player -> player.hasPermission(PERM.REPORT_ALERTS.name()))
                        .forEach(player ->
                        {
                            player.sendMessage(F.fStaff(this,
                                    F.fItem(sender.getName()),
                                    " reported ",
                                    F.fItem(target.getName()),
                                    ":") +
                                    "\n" +
                                    F.fStaff("", "Reason: ", F.fItem(reportData.reason._friendlyName)) +
                                    "\n" +
                                    F.fStaff("", "Server: ", F.fItem(reportData.server)));
                            player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                        });
            });
        });
    }

    @Override
    public void onDisable()
    {
        _reportGuis.keySet().forEach(HumanEntity::closeInventory);
        _reportGuis.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        _reportGuis.remove(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        _reportGuis.remove(event.getPlayer());
    }

    public void submitReport(Player reporter, OfflinePlayer target, ReportSubmitReason reason)
    {
        new ReportData(Map.ofEntries(Map.entry("reportUUID", UUID.randomUUID().toString()),
                Map.entry("senderUUID", reporter.getUniqueId().toString()),
                Map.entry("targetUUID", target.getUniqueId().toString()),
                Map.entry("reason", reason.name()),
                Map.entry("active", Boolean.toString(true)),
                Map.entry("origin", Long.toString(System.currentTimeMillis())),
                Map.entry("server", _corePortal._serverName))).submit(_coreDatabase._database._jedis);
    }

    public void openReportGui(Player reporter, OfflinePlayer target)
    {
        Inventory inventory = _hexusPlugin.getServer().createInventory(reporter, 3 * 9, "Report - " + target.getName());

        ItemStack targetSkull = UtilItem.createItemSkull(target.getName(),
                C.cGreen + C.fBold + target.getName(),
                target.getUniqueId().toString());

        ItemStack history = UtilItem.createItem(Material.NAME_TAG,
                C.cBlue + C.fBold + "Report History",
                "View the report history of " + F.fItem(target.getName()));

        ItemStack chat = UtilItem.createItem(Material.BOOK_AND_QUILL,
                C.cGreen + C.fBold + "Breaking Chat Rules",
                "Spamming",
                "Bigotry",
                "etc.");
        ItemStack gameplay = UtilItem.createItem(Material.IRON_BLOCK,
                C.cGreen + C.fBold + "Breaking Gameplay Rules",
                "Map Exploits",
                "Abusing Bugs",
                "etc.");
        ItemStack client = UtilItem.createItem(Material.IRON_SWORD,
                C.cGreen + C.fBold + "Unapproved Client Modifications",
                "Flying",
                "Xray",
                "etc.");
        ItemStack misc = UtilItem.createItem(Material.PAPER,
                C.cGreen + C.fBold + "Other Rule Violation",
                "User is breaking our rules in another way not listed here",
                "(We recommend also submitting a support ticket in this scenario)");

        inventory.setItem(4, targetSkull);
        inventory.setItem(10, chat);
        inventory.setItem(12, gameplay);
        inventory.setItem(14, client);
        inventory.setItem(16, misc);

        if (reporter.hasPermission(PermissionGroup.TRAINEE.name()))
        {
            inventory.setItem(26, history);
        }

        _reportGuis.put(reporter, new ReportGui(inventory, target, chat, gameplay, client, misc, history));
        reporter.openInventory(inventory);
    }

    public void openHistoryGui(Player reporter, OfflinePlayer target)
    {
        // TODO: Paginated report history
        reporter.sendMessage(F.fMain(this, "The report history GUI is still work in progress."));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player reporter))
        {
            return;
        }

        ReportGui reportGui = _reportGuis.get(reporter);
        if (reportGui == null)
        {
            return;
        }
        if (!reportGui.inventory().equals(event.getInventory()))
        {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem().equals(reportGui.history()))
        {
            openHistoryGui(reporter, reportGui._target());
            return;
        }

        AtomicReference<ReportSubmitReason> reportReason = new AtomicReference<>();
        if (event.getCurrentItem().equals(reportGui.chat()))
        {
            reportReason.set(ReportSubmitReason.CHAT);
        }
        else if (event.getCurrentItem().equals(reportGui.gameplay()))
        {
            reportReason.set(ReportSubmitReason.GAMEPLAY);
        }
        else if (event.getCurrentItem().equals(reportGui.client()))
        {
            reportReason.set(ReportSubmitReason.CLIENT);
        }
        else if (event.getCurrentItem().equals(reportGui.misc()))
        {
            reportReason.set(ReportSubmitReason.MISC);
        }
        else
        {
            return;
        }

        _hexusPlugin.runAsync(() ->
        {
            try
            {
                submitReport(reporter, reportGui._target(), reportReason.get());
            }
            catch (JedisException ex)
            {
                reporter.sendMessage(F.fMain(this,
                        F.fError("There was an error while submitting your report. Please try again later or contact " +
                                "an administrator if this issue persists.")));
                logSevere(ex);
                return;
            }

            reporter.sendMessage(F.fMain(this,
                    F.fSuccess("Your report has been successfully submitted and will be reviewed shortly.")));
        });
    }

}
