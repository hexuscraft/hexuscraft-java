package net.hexuscraft.hub.player;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.chat.C;
import net.hexuscraft.common.chat.F;
import net.hexuscraft.common.database.queries.ServerQueries;
import net.hexuscraft.common.database.serverdata.ServerData;
import net.hexuscraft.common.database.serverdata.ServerGroupData;
import net.hexuscraft.common.enums.PermissionGroup;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import redis.clients.jedis.UnifiedJedis;

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
    public void onLoad(
            final Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
        _pluginCommand = (MiniPluginCommand) dependencies.get(MiniPluginCommand.class);
        _miniPluginPortal = (MiniPluginPortal) dependencies.get(MiniPluginPortal.class);
        _miniPluginDatabase = (MiniPluginDatabase) dependencies.get(MiniPluginDatabase.class);
        _miniPluginPermission = (MiniPluginPermission) dependencies.get(MiniPluginPermission.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandSpawn(this));
        _actionTextTask = _hexusPlugin.getServer().getScheduler().runTaskTimer(_hexusPlugin,
                () -> _hexusPlugin.getServer().getOnlinePlayers().forEach(
                        player -> PlayerTabInfo.sendActionText(player, C.cYellow + C.fBold + "WWW.HEXUSCRAFT.NET")), 0,
                40);
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
        player.setPlayerListName(F.fPermissionGroup(PermissionGroup.getGroupWithHighestWeight(
                _miniPluginPermission._permissionProfiles.get(player)._groups()), true, true) + C.fReset + " " +
                player.getDisplayName());
    }

    @EventHandler
    void onPlayerQuit(final PlayerQuitEvent event) {
        event.setQuitMessage(F.fSub("Quit", event.getPlayer().getDisplayName()));
    }

    private void refreshInventory(Player player) {
        final PlayerInventory inventory = player.getInventory();

        final ItemStack gameCompass =
                UtilItem.createItem(Material.COMPASS, C.cGreen + C.fBold + "Game Menu", "Click to open the Game Menu");
        final ItemStack profileSkull = UtilItem.createItemSkull(player.getName(), C.cGreen + C.fBold + player.getName(),
                "Click to open the Profile Menu");
        final ItemStack cosmeticsChest = UtilItem.createItem(Material.CHEST, C.cGreen + C.fBold + "Cosmetics Menu",
                "Click to open the Cosmetics Menu");
        final ItemStack shopEmerald =
                UtilItem.createItem(Material.EMERALD, C.cGreen + C.fBold + "Shop Menu", "Click to open the Shop Menu");
        final ItemStack lobbyClock =
                UtilItem.createItem(Material.WATCH, C.cGreen + C.fBold + "Lobby Menu", "Click to open the Lobby Menu");

        inventory.clear();
        inventory.setItem(0, gameCompass);
        inventory.setItem(1, profileSkull);
        inventory.setItem(4, cosmeticsChest);
        inventory.setItem(7, shopEmerald);
        inventory.setItem(8, lobbyClock);
        inventory.setHeldItemSlot(0);
    }

    private void onItemInteract(final Player player, final ItemStack itemStack) {
        final Material itemType = itemStack.getType();

        if (!itemStack.hasItemMeta()) return;
        final ItemMeta currentItemMeta = itemStack.getItemMeta();

        if (!currentItemMeta.hasDisplayName()) return;
        final String displayName = currentItemMeta.getDisplayName();

        if (itemType.equals(Material.COMPASS) && displayName.contains("Game Menu")) {
            openGameMenu(player);
        } else if (itemType.equals(Material.SKULL_ITEM) && displayName.contains(player.getName())) {
            openProfileMenu(player);
        } else if (itemType.equals(Material.CHEST) && displayName.contains("Cosmetics Menu")) {
            openCosmeticsMenu(player);
        } else if (itemType.equals(Material.EMERALD) && displayName.contains("Shop Menu")) {
            openShopMenu(player);
        } else if (itemType.equals(Material.WATCH) && displayName.contains("Lobby Menu")) {
            openLobbyMenu(player);
        }
    }

    @EventHandler
    private void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        final Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            event.setCancelled(true);
            return;
        }

        if (clickedInventory.equals(player.getInventory())) {
            event.setCancelled(true);

            final ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) return;

            onItemInteract(player, currentItem);
            return;
        }

        if (clickedInventory.getName().equals("Lobby Menu")) {
            event.setCancelled(true);

            final ItemStack currentItem = event.getCurrentItem();
            if (!currentItem.hasItemMeta()) return;

            final ItemMeta currentItemMeta = currentItem.getItemMeta();
            if (!currentItemMeta.hasDisplayName()) return;

            _miniPluginPortal.teleportAsync(player, ChatColor.stripColor(currentItemMeta.getDisplayName()));
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2);
        }
    }

    void openGameMenu(final Player player) {
        player.sendMessage("open game");
    }

    void openProfileMenu(final Player player) {
        player.sendMessage("open profile");
    }

    void openCosmeticsMenu(final Player player) {
        player.sendMessage("open cosmetics");
    }

    void openShopMenu(final Player player) {
        player.sendMessage("open shop");
    }

    void openLobbyMenu(final Player player) {
        final Server server = _hexusPlugin.getServer();

        final Inventory lobbyMenu = server.createInventory(player, 54, "Lobby Menu");

        final BukkitScheduler scheduler = server.getScheduler();
        scheduler.runTaskAsynchronously(_hexusPlugin, () -> {
            final UnifiedJedis jedis = _miniPluginDatabase.getUnifiedJedis();

            final ServerGroupData lobbyGroupData = ServerQueries.getServerGroup(jedis, "Lobby");
            if (lobbyGroupData == null) return;

            final ServerData[] serverDataArray = ServerQueries.getServers(jedis, lobbyGroupData);
            scheduler.runTask(_hexusPlugin, () -> {
                for (ServerData serverData : serverDataArray) {
                    final int lobbyId = Integer.parseInt(serverData._name.split("-")[1]);
                    final boolean isCurrentServer = serverData._name.equals(_miniPluginPortal._serverName);

                    final ItemStack serverItem =
                            new ItemStack(isCurrentServer ? Material.EMERALD_BLOCK : Material.IRON_BLOCK);
                    serverItem.setAmount(lobbyId);

                    final ItemMeta serverItemMeta = serverItem.getItemMeta();
                    serverItemMeta.setDisplayName(C.cGreen + C.fBold + "Lobby-" + lobbyId);
                    serverItemMeta.setLore(List.of(C.cGray + (isCurrentServer ? "You are here" : "Click to join"), "",
                            F.fItem(serverData._players + "/" + serverData._capacity + " Players")));

                    serverItem.setItemMeta(serverItemMeta);
                    lobbyMenu.setItem(lobbyId - 1, serverItem);
                }
                player.openInventory(lobbyMenu);
            });
        });
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
