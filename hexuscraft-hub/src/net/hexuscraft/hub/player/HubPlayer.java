package net.hexuscraft.hub.player;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.enums.PermissionGroup;
import net.hexuscraft.common.utils.C;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilCooldown;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.command.CoreCommand;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.player.UtilTitleTab;
import net.hexuscraft.core.portal.CorePortal;
import net.hexuscraft.hub.Hub;
import net.hexuscraft.hub.player.command.CommandSpawn;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HubPlayer extends MiniPlugin<Hub>
{

    public enum PERM implements IPermission
    {
        COMMAND_SPAWN
    }

    CoreCommand _pluginCommand;
    CorePortal _corePortal;

    String _welcomeMessage = "";

    public HubPlayer(Hub hub)
    {
        super(hub, "Player");

        PermissionGroup._PLAYER._permissions.add(PERM.COMMAND_SPAWN);

        try
        {
            _welcomeMessage = String.join("\n", hub.readFile(new File("_welcome.dat")));
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies)
    {
        _pluginCommand = (CoreCommand) dependencies.get(CoreCommand.class);
        _corePortal = (CorePortal) dependencies.get(CorePortal.class);
    }

    @Override
    public void onEnable()
    {
        _pluginCommand.register(new CommandSpawn(this));
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event)
    {
        event.setJoinMessage(F.fSub("Join", event.getPlayer().getDisplayName()));

        Player player = event.getPlayer();
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

        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));

        if (_hexusPlugin._spawn != null)
        {
            player.teleport(_hexusPlugin._spawn);
        }

        refreshInventory(player);

        UtilTitleTab.sendHeaderFooter(player, F.fTabHeader(_corePortal._serverName), " ");

        sendWelcomeMessage(player);
    }

    void sendWelcomeMessage(Player player)
    {
        player.sendMessage(_welcomeMessage.replace("%player%", player.getDisplayName()));
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(F.fSub("Quit", event.getPlayer().getDisplayName()));
    }

    void refreshInventory(Player player)
    {
        PlayerInventory inventory = player.getInventory();

        ItemStack gameCompass =
                UtilItem.createItem(Material.COMPASS, C.cGreen + C.fBold + "Games", "Click to open the Games Menu");
        ItemStack profileSkull = UtilItem.createItemSkull(player.getName(),
                C.cGreen + C.fBold + player.getName(),
                "Click to open the Profile Menu");
        ItemStack cosmeticsChest = UtilItem.createItem(Material.CHEST,
                C.cGreen + C.fBold + "Cosmetics",
                "Click to open the Cosmetics Menu");
        ItemStack storeEmerald =
                UtilItem.createItem(Material.EMERALD, C.cGreen + C.fBold + "Store", "Click to open the Store Menu");
        ItemStack lobbyClock =
                UtilItem.createItem(Material.WATCH, C.cGreen + C.fBold + "Lobbies", "Click to open the Lobbies Menu");

        inventory.clear();
        inventory.setItem(0, gameCompass);
        inventory.setItem(1, profileSkull);
        inventory.setItem(4, cosmeticsChest);
        inventory.setItem(7, storeEmerald);
        inventory.setItem(8, lobbyClock);
        inventory.setHeldItemSlot(0);
    }

    boolean onItemInteract(Player player, ItemStack itemStack)
    {
        if (!itemStack.hasItemMeta())
        {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasDisplayName())
        {
            return false;
        }

        Material itemType = itemStack.getType();
        String displayName = ChatColor.stripColor(itemMeta.getDisplayName());

        if (itemType.equals(Material.COMPASS) && displayName.equals("Games"))
        {
            openGameMenu(player);
            return true;
        }
        if (itemType.equals(Material.SKULL_ITEM) && displayName.equals(player.getName()))
        {
            openProfileMenu(player);
            return true;
        }
        if (itemType.equals(Material.CHEST) && displayName.equals("Cosmetics"))
        {
            openCosmeticsMenu(player);
            return true;
        }
        if (itemType.equals(Material.EMERALD) && displayName.equals("Store"))
        {
            openStoreMenu(player);
            return true;
        }
        if (itemType.equals(Material.WATCH) && displayName.equals("Lobbies"))
        {
            openLobbyMenu(player);
            return true;
        }
        return false;
    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player player))
        {
            return;
        }
        if (player.getGameMode().equals(GameMode.CREATIVE))
        {
            return;
        }

        event.setCancelled(true);

        Inventory clickedInventory = event.getInventory();

        if (clickedInventory.equals(player.getInventory()))
        {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem == null)
            {
                return;
            }

            onItemInteract(player, currentItem);
            return;
        }
        if (ChatColor.stripColor(clickedInventory.getName()).equals("Lobby Menu"))
        {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem.hasItemMeta())
            {
                ItemMeta currentItemMeta = currentItem.getItemMeta();
                if (currentItemMeta.hasDisplayName())
                {
                    if (!UtilCooldown.use(player, "Lobby Server Teleport", 1000L))
                    {
                        return;
                    }

                    String targetServerName = ChatColor.stripColor(currentItemMeta.getDisplayName());
                    _corePortal.teleport(player, targetServerName);
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
                }
            }
        }
    }

    void openGameMenu(Player player)
    {
        player.sendMessage(F.fMain("Games", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openProfileMenu(Player player)
    {
        player.sendMessage(F.fMain("Profile", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openCosmeticsMenu(Player player)
    {
        player.sendMessage(F.fMain("Cosmetics", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openStoreMenu(Player player)
    {
        player.sendMessage(F.fMain("Shop", "This feature is currently work in progress. Please try again later!"));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    void openLobbyMenu(Player player)
    {
        Inventory lobbyMenu = _hexusPlugin.getServer().createInventory(player, 54, "Lobby Menu");

        Arrays.stream(_corePortal.getServers("Lobby")).limit(54).forEach(serverData ->
        {
            int lobbyId = Integer.parseInt(serverData._name.split(serverData._group + "-")[1]);
            if (lobbyId > 54)
            {
                return;
            }

            boolean isCurrentServer = serverData._name.equals(_corePortal._serverName);

            ItemStack serverItem = new ItemStack(isCurrentServer ? Material.EMERALD_BLOCK : Material.IRON_BLOCK);
            serverItem.setAmount(lobbyId);

            ItemMeta serverItemMeta = serverItem.getItemMeta();
            serverItemMeta.setDisplayName(C.cAqua + C.fBold + "Lobby-" + lobbyId);
            serverItemMeta.setLore(List.of(C.cDAqua + serverData._players + "/" + serverData._capacity + " Players",
                    "",
                    C.cYellow + C.fBold + (isCurrentServer ? "YOU ARE HERE" : "CLICK TO CONNECT")));

            serverItem.setItemMeta(serverItemMeta);
            lobbyMenu.setItem(lobbyId - 1, serverItem);
        });
        player.openInventory(lobbyMenu);
        player.playSound(player.getLocation(), Sound.NOTE_PLING, Float.MAX_VALUE, 2);
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player))
        {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM)
        {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID)
        {
            player.setVelocity(new Vector());
            player.teleport(_hexusPlugin._spawn);
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player))
        {
            return;
        }

        event.setCancelled(true);
        player.setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE))
        {
            return;
        }
        event.setCancelled(true);

        Item droppedItem = event.getItemDrop();
        if (droppedItem == null)
        {
            return;
        }

        ItemStack itemStack = droppedItem.getItemStack();
        if (itemStack == null)
        {
            return;
        }

        onItemInteract(player, itemStack);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE))
        {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE))
        {
            return;
        }
        event.setCancelled(true);

        ItemStack currentItem = player.getItemInHand();
        if (currentItem == null)
        {
            return;
        }

        onItemInteract(player, currentItem);
    }

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event)
    {
        if (!(event.getTarget() instanceof Player))
        {
            return;
        }
        event.setCancelled(true);
    }

}
