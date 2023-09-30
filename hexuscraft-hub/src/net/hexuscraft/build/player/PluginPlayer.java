package net.hexuscraft.build.player;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.PluginDatabase;
import net.hexuscraft.core.disguise.DisguiseEvent;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.permission.PluginPermission;
import net.hexuscraft.core.player.PlayerTabInfo;
import net.hexuscraft.core.portal.PluginPortal;
import net.hexuscraft.database.queries.ServerQueries;
import net.hexuscraft.database.serverdata.ServerData;
import net.hexuscraft.database.serverdata.ServerGroupData;
import net.hexuscraft.build.Hub;
import net.hexuscraft.build.player.command.CommandSpawn;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;

public class PluginPlayer extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_SPAWN
    }

    private PluginCommand _pluginCommand;
    private PluginPermission _pluginPermission;
    private PluginPortal _pluginPortal;
    private PluginDatabase _pluginDatabase;

    private final BukkitRunnable _actionTextTask;

    public PluginPlayer(Hub hub) {
        super(hub, "Player");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SPAWN);

        _actionTextTask = new BukkitRunnable() {
            @Override
            public void run() {
                _javaPlugin.getServer()
                        .getOnlinePlayers()
                        .forEach(player -> PlayerTabInfo.sendActionText(player, C.cYellow + C.fBold + "WWW.HEXUSCRAFT.NET"));
            }
        };
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin>, MiniPlugin> dependencies) {
        _pluginCommand = (PluginCommand) dependencies.get(PluginCommand.class);
        _pluginPermission = (PluginPermission) dependencies.get(PluginPermission.class);
        _pluginPortal = (PluginPortal) dependencies.get(PluginPortal.class);
        _pluginDatabase = (PluginDatabase) dependencies.get(PluginDatabase.class);
    }

    @Override
    public void onEnable() {
        _pluginCommand.register(new CommandSpawn(this));
        _actionTextTask.runTaskTimer(_javaPlugin, 0, 20);
    }

    @Override
    public void onDisable() {
        _actionTextTask.cancel();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Hub hub = (Hub) _javaPlugin;
        Location spawn = hub.getSpawn();

        Player player = getPlayer(event, hub);
        player.getInventory().setHeldItemSlot(0);
        refreshInventory(player);
        player.teleport(spawn);

        PlayerTabInfo.setHeaderFooter(player, F.fTabHeader(_pluginPortal._serverName), F.fTabFooter("WWW.HEXUSCRAFT.NET"));

        PermissionGroup primaryGroup = _pluginPermission._primaryGroupMap.get(player);
        event.setJoinMessage(F.fSub("Join") + F.fPermissionGroup(primaryGroup, true).toUpperCase() + " " + F.fItem(player.getName()));
    }

    private static Player getPlayer(PlayerJoinEvent event, Hub hub) {
        Player player = event.getPlayer();
        player.setFallDistance(0);
        player.setFlying(false);
        player.setSneaking(false);
        player.setAllowFlight(false);
        player.setGameMode(hub.getServer().getDefaultGameMode());
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setVelocity(new Vector());
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setExp(0);
        return player;
    }

    void refreshInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack gameCompass = UtilItem.createItem(Material.COMPASS, C.cGreen + C.fBold + "Game Menu", "Click to open the Game Menu");
        ItemStack profileSkull = UtilItem.createItemSkull(player.getName(), C.cGreen + C.fBold + player.getName(), "Click to open the Profile Menu");
        ItemStack cosmeticsChest = UtilItem.createItem(Material.CHEST, C.cGreen + C.fBold + "Cosmetics Menu", "Click to open the Cosmetics Menu");
        ItemStack shopEmerald = UtilItem.createItem(Material.EMERALD, C.cGreen + C.fBold + "Shop Menu", "Click to open the Shop Menu");
        ItemStack lobbyClock = UtilItem.createItem(Material.WATCH, C.cGreen + C.fBold + "Lobby Menu", "Click to open the Lobby Menu");

        inventory.clear();
        inventory.setItem(0, gameCompass);
        inventory.setItem(1, profileSkull);
        inventory.setItem(4, cosmeticsChest);
        inventory.setItem(7, shopEmerald);
        inventory.setItem(8, lobbyClock);
    }

    @EventHandler
    void onDisguise(DisguiseEvent event) {
        refreshInventory(event._player);
    }

    private void onItemInteract(final Player player, final ItemStack itemStack) {
        final Material itemType = itemStack.getType();

        if (!itemStack.hasItemMeta()) {
            return;
        }
        final ItemMeta currentItemMeta = itemStack.getItemMeta();

        if (!currentItemMeta.hasDisplayName()) {
            return;
        }
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
        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (event.getClickedInventory().equals(player.getInventory())) {
            event.setCancelled(true);

            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null) {
                return;
            }

            onItemInteract(player, currentItem);
            return;
        }

        if (event.getClickedInventory().getName().equals("Lobby Menu")) {
            event.setCancelled(true);

            final ItemStack currentItem = event.getCurrentItem();
            if (!currentItem.hasItemMeta()) {
                return;
            }

            final ItemMeta currentItemMeta = currentItem.getItemMeta();
            if (!currentItemMeta.hasDisplayName()) {
                return;
            }

            _pluginPortal.teleport(player.getName(), ChatColor.stripColor(currentItemMeta.getDisplayName()));
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
        final Inventory lobbyMenu = _javaPlugin.getServer().createInventory(player, 54, "Lobby Menu");

        final BukkitScheduler scheduler = _javaPlugin.getServer().getScheduler();
        scheduler.runTaskAsynchronously(_javaPlugin, () -> {
            final JedisPooled jedis = _pluginDatabase.getJedisPooled();
            final ServerGroupData lobbyGroupData = ServerQueries.getServerGroup(jedis, "lobby-main");
            final ServerData[] serverDataArray = ServerQueries.getServers(jedis, lobbyGroupData);
            scheduler.runTask(_javaPlugin, () -> {
                for (ServerData serverData : serverDataArray) {
                    final int lobbyId = Integer.parseInt(serverData._name.split("-")[1]);
                    final boolean isCurrentServer = serverData._name.equals(_pluginPortal._serverName);

                    final ItemStack serverItem = new ItemStack(isCurrentServer ? Material.EMERALD_BLOCK : Material.IRON_BLOCK);
                    serverItem.setAmount(lobbyId);

                    final ItemMeta serverItemMeta = serverItem.getItemMeta();
                    serverItemMeta.setDisplayName(C.cGreen + C.fBold + "Lobby-" + lobbyId);
                    serverItemMeta.setLore(List.of(
                            C.cGray + (isCurrentServer ? "You are here" : "Click to join"),
                            "",
                            F.fItem(serverData._players + "/" + serverData._capacity + " Players")
                    ));

                    serverItem.setItemMeta(serverItemMeta);
                    lobbyMenu.setItem(lobbyId - 1, serverItem);
                }
                player.openInventory(lobbyMenu);
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PermissionGroup primaryGroup = _pluginPermission._primaryGroupMap.get(player);
        event.setQuitMessage(F.fSub("Quit") + F.fPermissionGroup(primaryGroup, true).toUpperCase() + " " + F.fItem(player.getName()));
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        Hub hub = (Hub) _javaPlugin;
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.setVelocity(new Vector());
            player.teleport(hub.getSpawn());
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        event.setCancelled(true);
        player.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        event.setCancelled(true);

        Item droppedItem = event.getItemDrop();
        if (droppedItem == null) {
            return;
        }

        ItemStack itemStack = droppedItem.getItemStack();
        if (itemStack == null) {
            return;
        }

        onItemInteract(player, itemStack);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }
        event.setCancelled(true);

        ItemStack currentItem = player.getItemInHand();
        if (currentItem == null) {
            return;
        }

        onItemInteract(player, currentItem);
    }

}
