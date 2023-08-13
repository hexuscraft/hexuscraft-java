package net.hexuscraft.core.punish.command;

import net.hexuscraft.core.chat.C;
import net.hexuscraft.core.chat.F;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.item.UtilItem;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.player.MojangProfile;
import net.hexuscraft.core.player.PlayerSearch;
import net.hexuscraft.core.punish.PluginPunish;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandPunish extends BaseCommand {

    public CommandPunish(PluginPunish pluginPunish) {
        super(pluginPunish, "punishment", "<Player> <Reason>", "Open the punishment panel.", Set.of("punish", "x"), PluginPunish.PERM.COMMAND_PUNISH);
    }

    @Override
    public final void run(CommandSender sender, String alias, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(help(alias));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(F.fMain(this) + "Only players can run this command.");
            return;
        }

        sender.sendMessage(F.fMain(this) + "Fetching profile...");

        MojangProfile profile;
        try {
            profile = PlayerSearch.fetchMojangProfile(args[0]);
        } catch(IOException ex) {
            sender.sendMessage(F.fMain(this) + F.fError("Error while fetching profile of ") + F.fEntity(args[0]) + " (Did you type their name correctly?)");
            sender.sendMessage(F.fMain() + ex.getMessage());
            return;
        }

        String reasonMessage = String.join(" ", Arrays.stream(args).toList().subList(1, args.length));

        openGui(player, profile, reasonMessage);
    }

    @Override
    public List<String> tab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return _miniPlugin._javaPlugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }

    public final void openGui(Player player, MojangProfile targetProfile, String reasonMessage) {
        Inventory gui = _miniPlugin._javaPlugin.getServer().createInventory(player, 6*9, "Punish " + targetProfile.name);

        ItemStack devNotice = UtilItem.createDyeItem(DyeColor.YELLOW, C.cYellow + C.fBold + "Developer Notice", "Developers are advised against","using the punishment system","without consulting an " + F.fPermissionGroup(PermissionGroup.ADMINISTRATOR) + ".","","Exceptions allowed in emergency.");
        devNotice.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemStack targetSkull = UtilItem.createItemSkull(targetProfile.name, C.cGreen + C.fBold + targetProfile.name, targetProfile.uuid.toString(),"",C.cWhite + reasonMessage);

        ItemStack chatHeader = UtilItem.createItem(Material.BOOK_AND_QUILL, C.cBlue + C.fBold + "Chat Offenses");
        ItemStack chat1 = UtilItem.createItemWool(DyeColor.LIME, C.cGreen + C.fBold + "1 Day Mute", "Severity 1","Light chat offense","","Refer to guidelines for details.");
        ItemStack chat2 = UtilItem.createItemWool(DyeColor.YELLOW, C.cYellow + C.fBold + "3 Days Mute", "Severity 2","Moderate chat offense","","Refer to guidelines for details.");
        ItemStack chat3 = UtilItem.createItemWool(DyeColor.ORANGE, C.cRed + C.fBold + "5 Days Mute", "Severity 3","Heavy chat offense","","Refer to guidelines for details.");

        ItemStack gameplayHeader = UtilItem.createItem(Material.IRON_BLOCK, C.cBlue + C.fBold + "Gameplay Offenses");
        ItemStack gameplay1 = UtilItem.createItemWool(DyeColor.LIME, C.cGreen + C.fBold + "1 Day Ban", "Severity 1","Light gameplay offense","","Refer to guidelines for details.");
        ItemStack gameplay2 = UtilItem.createItemWool(DyeColor.YELLOW, C.cGreen + C.fBold + "3 Days Ban", "Severity 2","Moderate gameplay offense","","Refer to guidelines for details.");
        ItemStack gameplay3 = UtilItem.createItemWool(DyeColor.ORANGE, C.cGreen + C.fBold + "5 Days Ban", "Severity 3","Heavy gameplay offense","","Refer to guidelines for details.");

        ItemStack clientHeader = UtilItem.createItem(Material.IRON_SWORD, C.cBlue + C.fBold + "Client Offenses");
        ItemStack client1 = UtilItem.createItemWool(DyeColor.LIME, C.cGreen + C.fBold + "7 Days Ban", "Severity 1","Light client offense","","Refer to guidelines for details.");
        ItemStack client2 = UtilItem.createItemWool(DyeColor.YELLOW, C.cYellow + C.fBold + "14 Days Ban", "Severity 2","Moderate client offense","","Refer to guidelines for details.");
        ItemStack client3 = UtilItem.createItemWool(DyeColor.ORANGE, C.cGold + C.fBold + "30 Days Ban", "Severity 3","Heavy client offense","","Refer to guidelines for details.");

        ItemStack miscHeader = UtilItem.createItem(Material.LEVER, C.cBlue + C.fBold + "Miscellaneous");
        ItemStack miscWarn = UtilItem.createItem(Material.PAPER, C.cYellow + C.fBold + "Warning", "Severity 1","A friendly waring","","Refer to guidelines for details.");
        ItemStack miscMute = UtilItem.createItem(Material.BOOK, C.cRed + C.fBold + "Permanent Mute", "Severity 4","Severe chat offense","","Refer to guidelines for details.");
        ItemStack miscBan = UtilItem.createItem(Material.REDSTONE_BLOCK, C.cRed + C.fBold + "Permanent Ban", "Severity 4","Severe gameplay/client offense","","Refer to guidelines for details.");

        if (player.hasPermission(PermissionGroup.DEVELOPER.name())) {
            gui.setItem(0, devNotice);
        }
        gui.setItem(4, targetSkull);

        gui.setItem(10, chatHeader);
        gui.setItem(19, chat1);
        gui.setItem(28, chat2);
        gui.setItem(37, chat3);

        gui.setItem(12, gameplayHeader);
        gui.setItem(21, gameplay1);
        gui.setItem(30, gameplay2);
        gui.setItem(39, gameplay3);

        gui.setItem(14, clientHeader);
        gui.setItem(23, client1);
        gui.setItem(32, client2);
        gui.setItem(41, client3);

        gui.setItem(16, miscHeader);
        gui.setItem(25, miscWarn);
        gui.setItem(34, miscMute);
        gui.setItem(43, miscBan);

        player.openInventory(gui);
    }

}
