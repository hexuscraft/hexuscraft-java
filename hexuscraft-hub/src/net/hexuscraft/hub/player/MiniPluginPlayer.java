package net.hexuscraft.hub.player;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilCooldown;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.MiniPluginCommand;
import net.hexuscraft.core.database.MiniPluginDatabase;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.permission.MiniPluginPermission;
import net.hexuscraft.core.player.PlayerTabInfo;
import net.hexuscraft.core.portal.MiniPluginPortal;
import net.hexuscraft.hub.Hub;
import net.hexuscraft.hub.player.command.CommandSpawn;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class MiniPluginPlayer extends MiniPlugin<Hub> {

    private MiniPluginCommand _pluginCommand;
    private MiniPluginPortal _miniPluginPortal;
    private MiniPluginDatabase _miniPluginDatabase;
    private MiniPluginPermission _miniPluginPermission;
    private BukkitTask _actionTextTask;

    public MiniPluginPlayer(final Hub hub) {
        super(hub, "Player");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SPAWN);
    }

    @Override
    public void onLoad(final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandSpawn(this));
        _actionTextTask = _hexusPlugin.getServer().getScheduler().runTaskTimer(_hexusPlugin, () -> _hexusPlugin.getServer().getOnlinePlayers().forEach(player -> PlayerTabInfo.sendActionText(player, C.cYellow + C.fBold + "WWW.HEXUSCRAFT.NET")), 0, 40);
    }

    @Override
    public void onDisable() {
        if (_actionTextTask != null) _actionTextTask.cancel();
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        event.setJoinMessage(F.fSub("Join", event.getPlayer().getDisplayName()));

        final Player player = event.getPlayer();
        player.setFallDistance(0);
        player.setFlying(false);
        player.setSneaking(false);
        player.setAllowFlight(false);
        player.setGameMode(_hexusPlugin.getServer().getDefaultGameMode());
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setVelocity(new Vector());
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setExp(0);
        player.setFallDistance(0);

        if (_hexusPlugin._spawn != null) player.teleport(_hexusPlugin._spawn);

        refreshInventory(player);

        PlayerTabInfo.setHeaderFooter(player, F.fTabHeader(_miniPluginPortal._serverName), " ");

        player.sendMessage(F.fWelcomeMessage(player.getDisplayName()));
        player.setPlayerListName(F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(_miniPluginPermission._permissionProfiles.get(player)._groups()), true, true) + C.fReset + " " + player.getDisplayName());
    }

    @EventHandler
    void onPlayerQuit(final PlayerQuitEvent event) {
        event.setQuitMessage(F.fSub("Quit", event.getPlayer().getDisplayName()));
    }

    private void refreshInventory(Player player) {
        final PlayerInventory inventory = player.getInventory();

        final ItemStack gameCompass = UtilItem.createItem(Material.COMPASS, C.cGreen + C.fBold + "Game Menu", "Click to open the Game Menu");
        final ItemStack profileSkull = UtilItem.createItemSkull(player.getName(), C.cGreen + C.fBold + player.getName(), "Click to open the Profile Menu");
        final ItemStack cosmeticsChest = UtilItem.createItem(Material.CHEST, C.cGreen + C.fBold + "Cosmetics Menu", "Click to open the Cosmetics Menu");
        final ItemStack shopEmerald = UtilItem.createItem(Material.EMERALD, C.cGreen + C.fBold + "Shop Menu", "Click to open the Shop Menu");
        final ItemStack lobbyClock = UtilItem.createItem(Material.WATCH, C.cGreen + C.fBold + "Lobby Menu", "Click to open the Lobby Menu");

        inventory.clear();
        inventory.setItem(0, gameCompass);
        inventory.setItem(1, profileSkull);
        inventory.setItem(4, cosmeticsChest);
        inventory.setItem(7, shopEmerald);
        inventory.setItem(8, lobbyClock);
        inventory.setHeldItemSlot(0);
    }

    private boolean onItemInteract(final Player player, final ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return false;

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasDisplayName()) return false;

        final Material itemType = itemStack.getType();
        final String displayName = ChatColor.stripColor(itemMeta.getDisplayName());

        if (itemType.equals(Material.COMPASS) && displayName.equals("Game Menu")) {
            openGameMenu(player);
            return true;
        }
        if (itemType.equals(Material.SKULL_ITEM) && displayName.equals(player.getName())) {
            openProfileMenu(player);
            return true;
        }
        if (itemType.equals(Material.CHEST) && displayName.equals("Cosmetics Menu")) {
            openCosmeticsMenu(player);
            return true;
        }
        if (itemType.equals(Material.EMERALD) && displayName.equals("Shop Menu")) {
            openShopMenu(player);
            return true;
        }
        if (itemType.equals(Material.WATCH) && displayName.equals("Lobby Menu")) {
            openLobbyMenu(player);
            return true;
        }
        return false;
    }

    @EventHandler
    private void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        final Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            event.setCancelled(true);
        } else if (clickedInventory.equals(player.getInventory())) {
            event.setCancelled(true);

            final ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && onItemInteract(player, currentItem)) return;
        } else if (clickedInventory.getName().equals("Lobby Menu")) {
            event.setCancelled(true);
            final ItemStack currentItem = event.getCurrentItem();
            if (currentItem.hasItemMeta()) {
                final ItemMeta currentItemMeta = currentItem.getItemMeta();
                if (currentItemMeta.hasDisplayName()) {
                    if (!UtilCooldown.use(player, "Lobby Server Teleport", 1000L)) return;

                    final String targetServerName = ChatColor.stripColor(currentItemMeta.getDisplayName());
                    _miniPluginPortal.teleport(player, targetServerName);
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                }
            }
        }

        // Player has clicked an item (or empty space) in an inventory which does not return
        player.playSound(player.getLocation(), Sound.ITEM_BREAK, Float.MAX_VALUE, 0.5F);
    }

    void openGameMenu(final Player player) {
        player.sendMessage(F.fMain("Games", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openProfileMenu(final Player player) {
        player.sendMessage(F.fMain("Profile", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openCosmeticsMenu(final Player player) {
        player.sendMessage(F.fMain("Cosmetics", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openShopMenu(final Player player) {
        player.sendMessage(F.fMain("Shop", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openLobbyMenu(final Player player) {
        final Server server = _hexusPlugin.getServer();

        final Inventory lobbyMenu = server.createInventory(player, 54, "Lobby Menu");

        Arrays.stream(_miniPluginPortal.getServers("Lobby")).limit(54).forEach(serverData -> {
            final int lobbyId = Integer.parseInt(serverData._name.split(serverData._group + "-")[1]);
            if (lobbyId > 54) return;

            final boolean isCurrentServer = serverData._name.equals(_miniPluginPortal._serverName);

            final ItemStack serverItem = new ItemStack(isCurrentServer ? Material.EMERALD_BLOCK : Material.IRON_BLOCK);
            serverItem.setAmount(lobbyId);

            final ItemMeta serverItemMeta = serverItem.getItemMeta();
            serverItemMeta.setDisplayName(C.cGreen + C.fBold + "Lobby-" + lobbyId);
            serverItemMeta.setLore(List.of(C.cGray + (isCurrentServer ? "You are here" : "Click to join"), "", F.fItem(serverData._players + "/" + serverData._capacity + " Players")));

            serverItem.setItemMeta(serverItemMeta);
            lobbyMenu.setItem(lobbyId - 1, serverItem);
        });
        player.openInventory(lobbyMenu);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    @EventHandler
    void onEntityDamage(final EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof final Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.setVelocity(new Vector());
            player.teleport(_hexusPlugin._spawn);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof final Player player)) return;

        event.setCancelled(true);
        player.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);

        final Item droppedItem = event.getItemDrop();
        if (droppedItem == null) return;

        final ItemStack itemStack = droppedItem.getItemStack();
        if (itemStack == null) return;

        onItemInteract(player, itemStack);
    }

    @EventHandler
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);

        final ItemStack currentItem = player.getItemInHand();
        if (currentItem == null) return;

        onItemInteract(player, currentItem);
    }

    @EventHandler
    public void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        event.setCancelled(true);
    }

    public enum PERM implements IPermission {
        COMMAND_SPAWN
    }

}
