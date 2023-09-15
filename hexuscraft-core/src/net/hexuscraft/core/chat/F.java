package net.hexuscraft.core.chat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.anticheat.CheatSeverity;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.currency.CurrencyType;
import net.hexuscraft.core.permission.PermissionGroup;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class F {

    private static final String RESET = C.fReset;
    private static final String RESET_GRAY = C.fReset + C.cGray;

    private static final String SPACER = RESET + " ";
    private static final String SPACER_GRAY = RESET_GRAY + " ";

    private static final String PREFIX_MAIN = C.cBlue;
    private static final String PREFIX_SUB = C.cDGray;

    private static final String PREFIX_ANNOUNCE = C.cYellow;

    private static final String ITEM_COLOR = C.cYellow;

    public static String fMain(String prefix) {
        return PREFIX_MAIN + prefix + ">" + SPACER_GRAY;
    }

    public static String fMain() {
        return fMain("");
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
        return PREFIX_SUB + prefix + ">" + SPACER_GRAY;
    }

    public static String fSub() {
        return fSub("");
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
        return PREFIX_ANNOUNCE + "Announcement>" + SPACER + text;
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

    public static String fItem(Material material) {
        return fItem(material.name());
    }

    public static String fItem(Material material, int count) {
        return fItem(material.name(), count);
    }

    public static String fItem(ItemStack stack) {
        return fItem(stack.getType().name(), stack.getAmount());
    }

    public static String fList(String[] args) {
        return RESET_GRAY + "[" + ITEM_COLOR + String.join(RESET_GRAY + ", " + ITEM_COLOR, args) + RESET_GRAY + "]";
    }

    public static String fList(List<String> args) {
        return fList(args.toArray(String[]::new));
    }

    public static String fList(String text) {
        return text + "." + SPACER;
    }

    public static String fList(int index) {
        return fList(Integer.toString(index));
    }

    public static String fCurrency(String color, String nameSingular, String namePlural, int amount) {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural) + RESET_GRAY;
    }

    public static String fCurrency(CurrencyType currencyType, int amount) {
        return fCurrency(currencyType.getColor(), currencyType.getNameSingular(), currencyType.getNamePlural(), amount);
    }

    public static String fCommand(String alias, String usage, String description, String prefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append("/").append(alias);
        if (!usage.isEmpty()) {
            builder.append(" ").append(usage);
        }
        builder.append(SPACER_GRAY).append(description);
        return builder.toString();
    }

    public static String fCommand(String alias, String usage, String description) {
        return fCommand(alias, usage, description, C.cWhite);
    }

    public static String fCommand(BaseCommand command, String alias, String prefix) {
        return fCommand(alias, command.getUsage(), command.getDescription(), prefix);
    }

    public static String fCommand(BaseCommand command, String alias) {
        return fCommand(alias, command.getUsage(), command.getDescription());
    }

    public static String fChat(int level) {
        return C.cGray + level + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fChat(int level, PermissionGroup permissionGroup) {
        return C.cGray + level + SPACER + fPermissionGroup(permissionGroup, true).toUpperCase() + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fPermissionGroup(String prefix, String color) {
        return RESET + color + prefix + RESET_GRAY;
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
        return RESET + (toggle ? C.cGreen : C.cRed) + StringUtils.capitalize(Boolean.toString(toggle)) + RESET_GRAY;
    }

    public static String fSuccess(String action) {
        return RESET + C.cGreen + action + RESET_GRAY;
    }

    public static String fError(String action) {
        return RESET + C.cRed + action + RESET_GRAY;
    }

    public static String fPunishBan(UUID id, String reason, long length) {
        String formattedLength = length == 0 ? "Permanent" : fTime(length);
        return C.cRed + C.fBold + "You are banned for " + formattedLength + RESET + "\n"
                + reason + RESET + "\n"
                + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.hexuscraft.net" + RESET + "\n\n"
                + C.cDGray + id.toString();
    }

    public static String fCheat(String name, String color, String reason, int count, String... server) {
        StringBuilder builder = new StringBuilder(C.cAqua).append(C.fMagic).append("H")
                .append(SPACER)
                .append(C.cRed).append(C.fBold).append("HAC").append(" >")
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

    public static String fCheat(Player player, CheatSeverity severity, String reason, int count, String... server) {
        return fCheat(player.getName(), severity.getColor(), reason, count, server);
    }

    private static final double MILLIS_PER_SECOND = 1000;
    private static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    private static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

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
        return "\n     " + C.fBold + "Hexuscraft Network" + RESET + "     \n     " + C.cGreen + server + RESET + "     \n";
    }

    public static String fTabFooter(String website) {
        return RESET;
    }

}