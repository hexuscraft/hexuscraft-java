package net.hexuscraft.core.chat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.anticheat.CheatSeverity;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.currency.CurrencyType;
import net.hexuscraft.core.permission.PermissionGroup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class F {

    private static final String SPACER = C.fReset + " ";
    private static final String SPACER_GRAY = C.fReset + " " + C.cGray;
    private static final String RESET_GRAY = C.fReset + C.cGray;

    private static final String PREFIX_MAIN = C.cGold + C.fBold;
    //    static String PREFIX_SUB = C.cDGray + C.fBold;
    private static final String PREFIX_SUB = C.cDAqua + C.fBold;
    private static final String PREFIX_ANNOUNCE = C.cYellow + C.fBold;

    private static final String ITEM_COLOR = C.cYellow;

    public static String fMain(String prefix) {
//        return C.cBlue + C.fBold + prefix + SPACER_GRAY;
//        return PREFIX_MAIN + prefix + SPACER_GRAY;
        return C.cGray;
    }

    public static String fMain() {
//        return fMain("");
        return fMain(">");
    }

    public static String fMain(MiniPlugin miniPlugin) {
        return fMain(miniPlugin._name);
    }

    public static String fMain(BaseCommand baseCommand) {
        return fMain(baseCommand._miniPlugin);
    }

    public static String fMain(Player player) {
        return fMain(player.getDisplayName());
    }

    public static String fMain(Object object) {
        return fMain(object.toString());
    }

    public static String fMain(JavaPlugin plugin) {
        return fMain(plugin.getName());
    }

    public static String fSub(String prefix) {
//        return C.cDGray + C.fBold + prefix + SPACER_GRAY;
//        return PREFIX_SUB + prefix + SPACER_GRAY;
        return C.cGray;
    }

    public static String fSub() {
        return fSub(">");
    }

    public static String fSub(MiniPlugin miniPlugin) {
        return fSub(miniPlugin._name);
    }

    public static String fSub(BaseCommand baseCommand) {
        return fSub(baseCommand._miniPlugin);
    }

    public static String fSub(Player player) {
        return fSub(player.getDisplayName());
    }

    public static String fSub(Object object) {
        return fSub(object.toString());
    }

    public static String fSub(JavaPlugin plugin) {
        return fSub(plugin.getName());
    }

    public static String fStaff() {
        return fSub("[S]");
    }

    public static String fAnnounce(String text) {
//        return PREFIX_ANNOUNCE + text + SPACER;
        return text;
    }

    public static String fItem(String name) {
        return ITEM_COLOR + name + RESET_GRAY;
    }

    public static String fItem(String name, int count) {
        return ITEM_COLOR + count + " " + name + RESET_GRAY;
    }

    public static String fItem(CommandSender commandSender) {
        return fItem(commandSender.getName());
    }

    public static String fItem(Entity entity) {
        return fItem(entity.getName());
    }

//    public static String fItem(OfflinePlayer offlinePlayer) {
//        return fItem(offlinePlayer.getName());
//    }

    public static String fItem(Material material) {
        return fItem(material.name());
    }

    public static String fItem(Material material, int count) {
        return fItem(material.name(), count);
    }

    public static String fItem(ItemStack stack) {
        return fItem(stack.getType().name(), stack.getAmount());
    }

    public static String fCurrency(String color, String nameSingular, String namePlural, int amount) {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural) + RESET_GRAY;
    }

    public static String fCurrency(CurrencyType currencyType, int amount) {
        return fCurrency(currencyType.getColor(), currencyType.getNameSingular(), currencyType.getNamePlural(), amount);
    }

    public static String fBroadcast(String name, String msg) {
//        return C.cGold + C.fBold + name + SPACER + C.cYellow + msg;
        return fSub(name) + C.cAqua + msg;
    }

    public static String fBroadcast(CommandSender sender, String msg) {
        return fBroadcast(sender.getName(), msg);
    }

    public static String fCommand(String alias, String usage, String description, ChatColor color) {
        StringBuilder builder = new StringBuilder();
        builder.append(color).append("/").append(alias);
        if (!usage.isEmpty()) {
            builder.append(" ").append(usage);
        }
        builder.append(SPACER_GRAY).append(description);
        return builder.toString();
    }

    public static String fCommand(String alias, String usage, String description) {
        return fCommand(alias, usage, description, ChatColor.WHITE);
    }

    public static String fCommand(BaseCommand command, String alias, ChatColor color) {
        return fCommand(alias, command.getUsage(), command.getDescription(), color);
    }

    public static String fCommand(BaseCommand command, String alias) {
        return fCommand(alias, command.getUsage(), command.getDescription());
    }

    public static String fList(String... args) {
        return RESET_GRAY + "[" + ITEM_COLOR + String.join(RESET_GRAY + ", " + ITEM_COLOR, args) + RESET_GRAY + "]";
    }

    public static String fList(int index, String message) {
        return index + "." + SPACER + message;
    }

    public static String fChat(int level, PermissionGroup permissionGroup) {
        return C.cGray + level + SPACER + fPermissionGroup(permissionGroup, true).toUpperCase() + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fPermissionGroup(String prefix, String color) {
        return C.fReset + color + prefix + RESET_GRAY;
    }

    public static String fPermissionGroup(PermissionGroup permissionGroup) {
        return fPermissionGroup(permissionGroup._prefix, permissionGroup._color);
    }

    public static String fPermissionGroup(PermissionGroup permissionGroup, boolean bold) {
        StringBuilder builder = new StringBuilder(permissionGroup._color);
        if (bold) {
            builder.append(C.fBold);
        }
        return fPermissionGroup(permissionGroup._prefix, builder.toString());
    }

    public static String fInsufficientPermissions() {
        return fMain("Permissions") + "You have insufficient permissions to do this.";
    }

    public static String fBoolean(boolean toggle) {
        return C.fReset + (toggle ? (C.cGreen + "True") : (C.cRed + "False")) + RESET_GRAY;
    }

    public static String fBoolean(String action, boolean toggle) {
        return C.fReset + (toggle ? C.cGreen : C.cRed) + action + RESET_GRAY;
    }

    public static String fSuccess(String action) {
        return fBoolean(action, true);
    }

    public static String fError(String action) {
        return fBoolean(action, false);
    }

    public static String fPunishBan(UUID id, String reason, long length) {
        String formattedLength = length == 0 ? "Permanent" : fTime(length);
        return C.cRed + C.fBold + "You are banned for " + formattedLength + C.fReset
                + "\n" + reason + C.fReset
                + "\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.hexuscraft.net" + C.fReset
                + "\n"
                + "\n" + C.cDGray + id.toString();
    }

    public static String fCheat(String prefix, String name, String color, String reason, int count, String... server) {
        StringBuilder builder = new StringBuilder(C.cAqua).append(C.fMagic).append("H")
                .append(SPACER)
                .append(C.cRed).append(C.fBold).append(prefix.toUpperCase()).append(" >")
                .append(SPACER)
                .append(C.cGold).append(name)
                .append(SPACER)
                .append(C.cYellow).append("failed")
                .append(SPACER)
                .append(color).append(reason)
                .append(SPACER)
                .append("(").append(count).append(")");
        if (server.length > 0) {
            builder.append(SPACER)
                    .append(C.cYellow).append("in")
                    .append(SPACER)
                    .append(C.cAqua).append(server[0]);
        }
        return builder.toString();
    }

    public static String fCheat(String prefix, Player player, CheatSeverity severity, String reason, int count, String... server) {
        return fCheat(prefix, player.getName(), severity.getColor(), reason, count, server);
    }

    public static String fCheat(MiniPlugin miniPlugin, Player player, CheatSeverity severity, String reason, int count, String... server) {
        return fCheat(miniPlugin._name, player, severity, reason, count, server);
    }

    static final double MILLIS_PER_SECOND = 1000;
    static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    public static String fTime(long millis, int trim, TimeUnit unit) {
        if (millis == -1) {
            return "Permanent";
        }

        //noinspection ReassignedVariable
        String text;
        double time;
        if (unit == TimeUnit.DAYS) {
            text = (time = UtilMath.trim(millis / MILLIS_PER_DAY, trim)) + " Day";
        } else if (unit == TimeUnit.HOURS) {
            text = (time = UtilMath.trim(millis / MILLIS_PER_HOUR, trim)) + " Hour";
        } else if (unit == TimeUnit.MINUTES) {
            text = (time = UtilMath.trim(millis / MILLIS_PER_MINUTE, trim)) + " Minute";
        } else if (unit == TimeUnit.SECONDS) {
            text = (time = UtilMath.trim(millis / MILLIS_PER_SECOND, trim)) + " Second";
        } else {
            text = (time = UtilMath.trim(millis)) + " Millisecond";
        }

        if (time != 1) {
            text += "s";
        }

        return text;
    }

    public static String fTime(long millis, int trim) {
        if (millis < MILLIS_PER_MINUTE) {
            return fTime(millis, trim, TimeUnit.SECONDS);
        }
        if (millis < MILLIS_PER_HOUR) {
            return fTime(millis, trim, TimeUnit.MINUTES);
        }
        if (millis < MILLIS_PER_DAY) {
            return fTime(millis, trim, TimeUnit.HOURS);
        }
        return fTime(millis, trim, TimeUnit.DAYS);
    }

    public static String fTime(long millis) {
        return fTime(millis, 1);
    }

    public static String fTabHeader(String server) {
        return C.cWhite + C.fBold + "Hexuscraft Network" + SPACER + " " + C.cGreen + server;
    }

    public static String fTabFooter(String website) {
        return C.cWhite + "Visit" + SPACER + C.cGreen + website + SPACER + "for News, Forums and Shop";
    }

}