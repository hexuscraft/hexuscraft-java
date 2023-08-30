package net.hexuscraft.hub.player;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.PluginCommand;
import net.hexuscraft.core.database.AsyncRunnable;
import net.hexuscraft.core.database.ParameterizedRunnable;
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
import net.hexuscraft.hub.Hub;
import net.hexuscraft.hub.player.command.CommandSpawn;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.util.Vector;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PluginPlayer extends MiniPlugin {

    public enum PERM implements IPermission {
        COMMAND_SPAWN
    }

    PluginCommand _pluginCommand;
    PluginPermission _pluginPermission;
    PluginPortal _pluginPortal;
    PluginDatabase _pluginDatabase;

    BukkitRunnable _actionTextTask;

    public PluginPlayer(Hub hub) {
        super(hub, "Player");

        PermissionGroup.MEMBER._permissions.add(PERM.COMMAND_SPAWN);
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

        _actionTextTask = new BukkitRunnable() {

            @Override
            public void run() {
                _javaPlugin.getServer().getOnlinePlayers().forEach(player -> PlayerTabInfo.sendActionText(player, C.cYellow + C.fBold + _pluginPortal._serverWebsite.toUpperCase()));
            }

        };
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
        player.getInventory().setHeldItemSlot(0);
        refreshInventory(player);
        player.teleport(spawn);

        PlayerTabInfo.setHeaderFooter(player, F.fTabHeader(_pluginPortal._serverName), F.fTabFooter(_pluginPortal._serverWebsite));

        PermissionGroup primaryGroup = _pluginPermission._primaryGroupMap.get(player);
        event.setJoinMessage(F.fSub("Join") + F.fPermissionGroup(primaryGroup, true).toUpperCase() + " " + F.fItem(player.getName()));
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

    void onItemInteract(Player player, ItemStack itemStack) {
        Material itemType = itemStack.getType();

        if (!itemStack.hasItemMeta()) {
            return;
        }
        ItemMeta currentItemMeta = itemStack.getItemMeta();

        if (!currentItemMeta.hasDisplayName()) {
            return;
        }
        String displayName = currentItemMeta.getDisplayName();

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
    void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
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

            ItemStack currentItem = event.getCurrentItem();
            if (!currentItem.hasItemMeta()) {
                return;
            }

            ItemMeta currentItemMeta = currentItem.getItemMeta();
            if (!currentItemMeta.hasDisplayName()) {
                return;
            }

            final int lobbyId = Integer.parseInt(currentItemMeta.getDisplayName().split("Lobby ", 2)[1]);
            if (_pluginPortal._serverName.equals("Lobby-" + lobbyId)) {
                player.playSound(player.getLocation(), Sound.ANVIL_LAND, Integer.MAX_VALUE, 0.8F);
                return;
            }

            _pluginPortal.teleport(player.getName(), "Lobby-" + lobbyId);
        }

    }

    void openGameMenu(Player player) {
        player.sendMessage("open game");
    }

    void openProfileMenu(Player player) {
        player.sendMessage("open profile");
    }

    void openCosmeticsMenu(Player player) {
        player.sendMessage("open cosmetics");
    }

    void openShopMenu(Player player) {
        player.sendMessage("open shop");
    }

    void openLobbyMenu(Player player) {
        Inventory lobbyMenu = _javaPlugin.getServer().createInventory(player, 54, "Lobby Menu");

        new AsyncRunnable(this, new ParameterizedRunnable() {
            @Override
            public Object run(Object... params) {
                JedisPooled jedis = _pluginDatabase.getJedisPooled();
                return jedis.smembers(ServerQueries.SERVERS_ACTIVE())
                        .stream()
                        .map(UUID::fromString)
                        .map(uuid -> new ServerData(uuid, jedis.hgetAll(ServerQueries.SERVER(uuid))))
                        .toArray(Object[]::new);
            }
        }, new ParameterizedRunnable() {
            @Override
            public Object run(Object... params) {
                ServerData[] lobbyServers = (ServerData[]) params[0];
                for (ServerData serverData : lobbyServers) {
                    final int lobbyId = Integer.parseInt(serverData._name.split("-")[1]);
                    final boolean isCurrentServer = serverData._name.equals(_pluginPortal._serverName);

                    ItemStack serverItem = new ItemStack(isCurrentServer ? Material.EMERALD_BLOCK : Material.IRON_BLOCK);
                    serverItem.setAmount(lobbyId);

                    ItemMeta serverItemMeta = serverItem.getItemMeta();
                    serverItemMeta.setDisplayName(C.cGreen + C.fBold + "Lobby " + lobbyId);
                    serverItemMeta.setLore(List.of(
                            C.cGray + (isCurrentServer ? "You are here" : "Click to join"),
                            "",
                            F.fItem(serverData._playerCount + "/" + serverData._maxPlayers + " Players")
                    ));

                    serverItem.setItemMeta(serverItemMeta);
                    lobbyMenu.setItem(lobbyId - 1, serverItem);
                }
                player.openInventory(lobbyMenu);
                return null;
            }
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
