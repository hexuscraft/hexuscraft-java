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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Punishment handling for Minecraft servers.
 * Not responsible for removing players - that is handled by Proxies.
 */
public final class CorePunish extends MiniPlugin<HexusPlugin> {

	// Constants

	final long ONE_DAY_MILLIS = 86400000;

	// Variables

	final Map<HumanEntity, PunishGui> _punishGuis;
	final Map<HumanEntity, PunishHistoryGui> _punishHistoryGuis;

	// Dependencies

	CoreActionBar _coreActionBar;
	CoreCommand _coreCommand;
	CoreDatabase _coreDatabase;
	CorePortal _corePortal;

	// Constructors

	public CorePunish(HexusPlugin plugin) {
		super(plugin, "Punish");

		_punishGuis = new HashMap<>();
		_punishHistoryGuis = new HashMap<>();
	}

	// Core Methods

	@Override
	public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
		_coreActionBar = (CoreActionBar) dependencies.get(CoreActionBar.class);
		_coreCommand = (CoreCommand) dependencies.get(CoreCommand.class);
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
	public void onEnable() {
		_coreCommand.register(new CommandRules(this));
		_coreCommand.register(new CommandPunish(this));
		_coreCommand.register(new CommandPunishHistory(this));

		_coreDatabase._database.registerConsumer(PunishAppliedMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
			_hexusPlugin.runAsync(() -> {
				final UUID uuid = PunishAppliedMessage.fromString(rawMessage)._uuid;
				final PunishData punishData;
				try {
					punishData = new PunishData(_coreDatabase._database._jedis.hgetAll(uuid.toString()));
				} catch (JedisException ex) {
					logSevere(ex);
					return;
				}

				final OfflinePlayer target = _hexusPlugin.getServer().getPlayer(punishData._targetUUID);
				if (target == null) return;

				final String staffName;
				{
					if (punishData._staffUUID.equals(UtilUniqueId.EMPTY_UUID)) {
						staffName = "<CONSOLE>";
					} else {
						OfflinePlayer punisher;
						try {
							punisher = PlayerSearch.offlinePlayerSearch(punishData._staffUUID);
						} catch (URISyntaxException | IOException ex) {
							logSevere(ex);
							return;
						}
						staffName = punisher == null ? "<UNKNOWN>" : punisher.getName();
					}
				}

				final String punishMessage = F.fPunish(punishData);
				if (target.isOnline()) {
					Player targetPlayer = target.getPlayer();
					targetPlayer.sendMessage(punishMessage);
					targetPlayer.playSound(targetPlayer.getLocation(), Sound.CAT_MEOW, Float.MAX_VALUE, 0.6F);
				}

				_hexusPlugin.getServer().getOnlinePlayers().stream().filter((final Player staff) -> staff.hasPermission(PERM.PUNISH_ALERTS.name())).forEach((final Player staff) -> {
					switch (punishData._type) {
						case PunishType.WARNING ->
							staff.sendMessage(F.fStaff(this, F.fItem(staffName), " warned ", F.fItem(target.getName()), ":\n", F.fStaff("", "Reason: ", F.fItem(punishData._reason))));
						case PunishType.KICK ->
							staff.sendMessage(F.fStaff(this, F.fItem(staffName), " kicked ", F.fItem(target.getName()), ":\n", F.fStaff("", "Reason: ", F.fItem(punishData._reason))));
						case PunishType.MUTE ->
							staff.sendMessage(F.fStaff(this, F.fItem(staffName), " muted ", F.fItem(target.getName()), " for ", F.fItem(F.fTime(punishData._length)), ":\n", F.fStaff("", "Reason: ", F.fItem(punishData._reason))));
						case PunishType.BAN ->
							staff.sendMessage(F.fStaff(this, F.fItem(staffName), " banned ", F.fItem(target.getName()), " for ", F.fItem(F.fTime(punishData._length)), ":\n", F.fStaff("", "Reason: ", F.fItem(punishData._reason))));
					}
					staff.playSound(staff.getLocation(), Sound.CAT_MEOW, Float.MAX_VALUE, 0.6F);
				});
			});
		});
	}

	@Override
	public void onDisable() {
		_coreActionBar = null;
		_coreCommand = null;
		_coreDatabase = null;
		_corePortal = null;
		_punishGuis.clear();
		_punishHistoryGuis.clear();
	}

	// Class Methods

	public BukkitTask punishAsync(UUID targetUUID, UUID staffUUID, PunishType punishType, long lengthMillis, String reason) {
		return punishAsync(targetUUID, staffUUID, punishType, lengthMillis, reason, null);
	}

	public BukkitTask punishAsync(UUID targetUUID, UUID staffUUID, PunishType punishType, long lengthMillis, String reason, Consumer<PunishData> callback) {
		PunishData punishData = new PunishData(UUID.randomUUID(), punishType, true, System.currentTimeMillis(), lengthMillis, reason, targetUUID, _corePortal._serverName, staffUUID, _corePortal._serverName);

		return _hexusPlugin.runAsync(() -> {
			try {
				punishData.publish(_coreDatabase._database._jedis);
			} catch (JedisException ex) {
				logSevere(ex);
				callback.accept(null);
				return;
			}
			if (callback != null) {
				callback.accept(punishData);
			}
		});
	}

	@SuppressWarnings("deprecation")
	public void openPunishGui(Player staff, OfflinePlayer target, String reason) {

		// Construct inventory & items

		Inventory inventory = _hexusPlugin.getServer().createInventory(staff, 6 * 9, "Punish - " + target.getName());

		ItemStack skull = UtilItem.createPlayerSkull(target.getName(), C.cGreen + C.fBold + target.getName(), target.getUniqueId().toString(), "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO VIEW HISTORY");

		ItemStack warning = UtilItem.create(Material.PAPER, C.cGreen + C.fBold + "Friendly Warning", "Inform someone that they are breaking the rules", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO ISSUE WARNING");

		ItemStack mute1d = UtilItem.createWithData(Material.WOOL, DyeColor.LIME.getData(), C.cGreen + C.fBold + "Chat Offense", "Severity 1", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 1 DAY");
		ItemStack mute3d = UtilItem.createWithData(Material.WOOL, DyeColor.LIME.getData(), C.cGreen + C.fBold + "Chat Offense", "Severity 1", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 3 DAYS");
		ItemStack mute5d = UtilItem.createWithData(Material.WOOL, DyeColor.YELLOW.getData(), C.cYellow + C.fBold + "Chat Offense", "Severity 2", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 5 DAYS");
		ItemStack mute7d = UtilItem.createWithData(Material.WOOL, DyeColor.YELLOW.getData(), C.cYellow + C.fBold + "Chat Offense", "Severity 2", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 7 DAYS");
		ItemStack mute14d = UtilItem.createWithData(Material.WOOL, DyeColor.ORANGE.getData(), C.cGold + C.fBold + "Chat Offense", "Severity 3", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 14 DAYS");
		ItemStack mute28d = UtilItem.createWithData(Material.WOOL, DyeColor.ORANGE.getData(), C.cGold + C.fBold + "Chat Offense", "Severity 3", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR 28 DAYS");
		ItemStack mutePerm = UtilItem.createWithData(Material.WOOL, DyeColor.RED.getData(), C.cRed + C.fBold + "Chat Offense", "Severity 4", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO MUTE FOR PERMANENT");

		ItemStack ban1d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.LIME.getData(), C.cGreen + C.fBold + "Gameplay Offense", "Severity 1", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 1 DAY");
		ItemStack ban3d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.LIME.getData(), C.cGreen + C.fBold + "Gameplay Offense", "Severity 1", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 3 DAYS");
		ItemStack ban5d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.YELLOW.getData(), C.cYellow + C.fBold + "Gameplay Offense", "Severity 2", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 5 DAYS");
		ItemStack ban7d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.YELLOW.getData(), C.cYellow + C.fBold + "Gameplay Offense", "Severity 2", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 7 DAYS");
		ItemStack ban14d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.ORANGE.getData(), C.cGold + C.fBold + "Gameplay Offense", "Severity 3", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 14 DAYS");
		ItemStack ban28d = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.ORANGE.getData(), C.cGold + C.fBold + "Gameplay Offense", "Severity 3", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR 28 DAYS");
		ItemStack banPerm = UtilItem.createWithData(Material.STAINED_CLAY, DyeColor.RED.getData(), C.cRed + C.fBold + "Gameplay Offense", "Severity 4", "", C.cGray + C.fBold + "Provided Reason", "  " + C.cWhite + reason, "", C.cYellow + C.fBold + "CLICK TO BAN FOR PERMANENT");

		// Set items

		inventory.setItem(12, skull);

		if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
			inventory.setItem(14, warning);
			inventory.setItem(28, mute1d);
			inventory.setItem(29, mute3d);
			inventory.setItem(37, ban1d);
			inventory.setItem(38, ban3d);
		}

		if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name())) {
			inventory.setItem(30, mute5d);
			inventory.setItem(31, mute7d);
			inventory.setItem(39, ban5d);
			inventory.setItem(40, ban7d);
		}

		if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name())) {
			inventory.setItem(32, mute14d);
			inventory.setItem(33, mute28d);
			inventory.setItem(41, ban14d);
			inventory.setItem(42, ban28d);
		}

		if (staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name())) {
			inventory.setItem(34, mutePerm);
			inventory.setItem(43, banPerm);
		}

		_punishGuis.put(staff, new PunishGui(inventory, target, reason, skull, warning, mute1d, mute3d, mute5d, mute7d, mute14d, mute28d, mutePerm, ban1d, ban3d, ban5d, ban7d, ban14d, ban28d, banPerm));
		staff.openInventory(inventory);
	}

	public void openHistoryGui(Player viewer, OfflinePlayer target) {
		Inventory inventory = _hexusPlugin.getServer().createInventory(viewer, 6 * 9, "Punish History - " + target.getName());
		inventory.setItem(4, UtilItem.createPlayerSkull(target.getName(), C.cGreen + C.fBold + target.getName(), target.getUniqueId().toString(), "", C.cWhite + "Viewing punishment history"));

		AtomicInteger loadingIndex = new AtomicInteger();
		String loadingDisplayName = C.cGold + C.fBold + "Loading Punishments";
		ItemStack loadingActiveIndex = UtilItem.create(Material.EMERALD_BLOCK, loadingDisplayName);
		ItemStack loadingInactiveIndex = UtilItem.create(Material.IRON_BLOCK, loadingDisplayName);
		BukkitTask loadingTask = _hexusPlugin.runAsyncTimer(() -> {
			int index = loadingIndex.getAndUpdate(operand -> {
				if (operand >= 2) {
					return 0;
				}
				return operand + 1;
			});

			inventory.setItem(30, index == 0 ? loadingActiveIndex : loadingInactiveIndex);
			inventory.setItem(31, index == 1 ? loadingActiveIndex : loadingInactiveIndex);
			inventory.setItem(32, index == 2 ? loadingActiveIndex : loadingInactiveIndex);
		}, 0, 5);

		Map<ItemStack, PunishData> punishments = new HashMap<>();
		BukkitTask fetchTask = _hexusPlugin.runAsyncLater(() -> {
			PunishData[] punishDatas;
			try {
				punishDatas = _coreDatabase._database._jedis.smembers(PunishQueries.RECEIVED(target.getUniqueId())).stream().map(UUID::fromString).map(punishmentUUID -> new PunishData(_coreDatabase._database._jedis.hgetAll(PunishQueries.PUNISHMENT(punishmentUUID)))).sorted(Comparator.comparingLong(punishData -> -punishData._origin))
					// Sorting by negative origin makes the newest punishments appear first
					.toArray(PunishData[]::new);
			} catch (JedisException ex) {
				viewer.sendMessage(F.fMain(this, F.fError("Failed to fetch punishments of ", F.fItem(target.getName()), ". Please try again later or contact an administrator if this issue persists.")));
				logSevere(ex);
				return;
			} finally {
				loadingTask.cancel();
			}

			for (int i = 9; i < 54; i++)
				inventory.setItem(i, null);

			for (int i = 0; i < punishDatas.length; i++) {
				if (i >= 9 * 6 - 1) {
					break;
				}
				PunishData punishData = punishDatas[i];
				Material punishmentItemMaterial;
				switch (punishData._type) {
					case BAN -> punishmentItemMaterial = Material.IRON_BLOCK;
					case KICK -> punishmentItemMaterial = Material.STICK;
					case WARNING -> punishmentItemMaterial = Material.PAPER;
					case MUTE -> punishmentItemMaterial = Material.BOOK_AND_QUILL;
					default -> punishmentItemMaterial = Material.GRASS;
				}

				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				List<String> lore = new ArrayList<>(List.of("Reason: " + F.fItem(punishData._reason)));
				if (punishData._length != -1)
					lore.add("Expires: " + F.fItem(F.fTime(punishData.getRemaining())) + C.cGray + " (" + F.fItem(dateFormatter.format(new Date(punishData._origin))) + C.cGray + ")");
				if (!punishData._active)
					lore.addAll(List.of("", "Remove reason: " + F.fItem(punishData._removeReason), "Removed at: " + F.fItem(dateFormatter.format(new Date(punishData._removeOrigin)))));

				String displayName = C.fBold + F.fTime(punishData._length) + " " + punishData._type._friendlyName;
				ItemStack punishmentItem = UtilItem.create(punishmentItemMaterial, punishData._active ? F.fSuccess(displayName) : F.fError(displayName), lore.toArray(String[]::new));

				inventory.setItem(i + 9, punishmentItem);
			}
		}, 1);

		_punishHistoryGuis.put(viewer, new PunishHistoryGui(inventory, target, loadingTask, fetchTask, punishments));
		viewer.openInventory(inventory);
	}

	@EventHandler
	void onInventoryClose(InventoryCloseEvent event) {
		HumanEntity player = event.getPlayer();

		if (_punishHistoryGuis.containsKey(player)) {
			_punishHistoryGuis.get(player)._loadingTask().cancel();
		}

		_punishGuis.remove(player);
		_punishHistoryGuis.remove(player);
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player staff)) {
			return;
		}

		PunishGui punishGui = _punishGuis.get(staff);
		if (punishGui != null && punishGui._inventory().equals(event.getInventory())) {
			event.setCancelled(true);

			AtomicReference<PunishType> type = new AtomicReference<>();
			AtomicLong lengthMillis = new AtomicLong(-1);

			ItemStack item = event.getCurrentItem();

			if (item.equals(punishGui._skull())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_HISTORY.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				openHistoryGui(staff, punishGui._target());
			} else if (item.equals(punishGui._warning())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.WARNING);
			} else if (item.equals(punishGui._mute1d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS);
			} else if (item.equals(punishGui._ban1d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS);
			} else if (item.equals(punishGui._mute3d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS * 3);
			} else if (item.equals(punishGui._ban3d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_1.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS * 3);
			} else if (item.equals(punishGui._mute5d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS * 5);
			} else if (item.equals(punishGui._ban5d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS * 5);
			} else if (item.equals(punishGui._mute7d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS * 7);
			} else if (item.equals(punishGui._ban7d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_2.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS * 7);
			} else if (item.equals(punishGui._mute14d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS * 14);
			} else if (item.equals(punishGui._ban14d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS * 14);
			} else if (item.equals(punishGui._mute28d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
				lengthMillis.set(ONE_DAY_MILLIS * 28);
			} else if (item.equals(punishGui._ban28d())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_3.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
				lengthMillis.set(ONE_DAY_MILLIS * 28);
			} else if (item.equals(punishGui._mutePerm())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.MUTE);
			} else if (item.equals(punishGui._banPerm())) {
				if (!staff.hasPermission(PERM.COMMAND_PUNISH_SEVERITY_4.name())) {
					staff.sendMessage(F.fInsufficientPermissions());
					return;
				}
				type.set(PunishType.BAN);
			}

			if (type.get() == null) {
				return;
			}

			ActionBar actionBar = _coreActionBar.registerActionBar(new ActionBar(_coreActionBar, staff, 1, F.fActionBar(this, "Processing your punishment against ", F.fItem(punishGui._target().getName()), "...")));

			staff.closeInventory();
			punishAsync(punishGui._target().getUniqueId(), staff.getUniqueId(), type.get(), lengthMillis.get(), punishGui._reason(), (punishData -> {
				actionBar.setMessage(F.fActionBar(this, F.fSuccess("Punishment successfully applied against ", F.fItem(punishGui._target().getName()), ".")));
				_coreActionBar.unregisterActionBar(actionBar);
				if (punishData != null) {
					return;
				}
				staff.sendMessage(F.fMain(this, F.fError("There was an error while processing the punishment. Please try again " + "later or contact an administrator if this issue persists.")));
			}));
			return;
		}

		PunishHistoryGui punishHistoryGui = _punishHistoryGuis.get(staff);
		if (punishHistoryGui != null && punishHistoryGui._inventory().equals(event.getInventory())) {
			event.setCancelled(true);
			// TODO: Punish history GUI. Click to remove punishment.
		}
	}

	public enum PERM implements IPermission {
		COMMAND_PUNISH, COMMAND_PUNISH_SEVERITY_1, COMMAND_PUNISH_SEVERITY_2, COMMAND_PUNISH_SEVERITY_3, COMMAND_PUNISH_SEVERITY_4, COMMAND_PUNISH_HISTORY, COMMAND_RULES, PUNISH_ALERTS,
	}
}