package net.hexuscraft.core.chat;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.anticheat.CheatSeverity;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.common.UtilMath;
import net.hexuscraft.core.currency.CurrencyType;
import net.hexuscraft.core.permission.PermissionGroup;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class F {

    private static final String RESET = C.fReset;
    private static final String RESET_GRAY = C.fReset + C.cGray;

    private static final String SPACER = RESET + " ";
    private static final String SPACER_GRAY = RESET_GRAY + " ";

    private static final String ITEM_COLOR = C.cYellow;

    public static String fMain(final String prefix, final String... text) {
        return C.cBlue + prefix + ">" + SPACER_GRAY + String.join("", text);
    }

    public static String fMain(final MiniPlugin<?> miniPlugin, final String... text) {
        return fMain(miniPlugin._name, text);
    }

    public static String fMain(final BaseCommand baseCommand, final String... text) {
        return fMain(baseCommand._miniPlugin, text);
    }

    public static String fMain(final Player player, final String... text) {
        return fMain(player.getDisplayName(), text);
    }

    public static String fMain(final Object object, final String... text) {
        return fMain(object.toString(), text);
    }


    public static String fSub(final String prefix, final String... text) {
        return fMain(C.cDGray + prefix, text);
    }

    public static String fSub(final MiniPlugin<?> miniPlugin, final String... text) {
        return fSub(miniPlugin._name, text);
    }

    public static String fSub(final BaseCommand baseCommand, final String... text) {
        return fSub(baseCommand._miniPlugin, text);
    }

    public static String fSub(final Object object, final String... text) {
        return fSub(object.toString(), text);
    }


    public static String fItem(final String name) {
        return ITEM_COLOR + name + RESET_GRAY;
    }

    public static String fItem(final String name, final int count) {
        return fItem(count + " " + name);
    }

    public static String fItem(final CommandSender commandSender) {
        return fItem(commandSender.getName());
    }

    public static String fItem(final Entity entity) {
        return fItem(entity.getName());
    }

    public static String fItem(final Material material) {
        return fItem(material.name());
    }

    public static String fItem(final Material material, int count) {
        return fItem(material.name(), count);
    }

    public static String fItem(final ItemStack stack) {
        return fItem(stack.getType().name(), stack.getAmount());
    }


    public static String fList(final String[] args) {
        return RESET_GRAY + "[" + fItem(String.join(RESET_GRAY + ", " + ITEM_COLOR, args)) + "]";
    }

    public static String fList(final List<String> args) {
        return fList(args.toArray(String[]::new));
    }

    public static String fList(final String text) {
        return text + "." + SPACER;
    }

    public static String fList(final int index) {
        return fList(Integer.toString(index));
    }


    public static String fCurrency(final String color, final String nameSingular, final String namePlural, final int amount) {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural) + RESET_GRAY;
    }

    @SuppressWarnings("unused")
    public static String fCurrency(final CurrencyType currencyType, final int amount) {
        return fCurrency(currencyType._color, currencyType._nameSingular, currencyType._namePlural, amount);
    }


    public static String fCommand(final String alias, final String usage, final String description, final String prefix) {
        final StringBuilder builder = new StringBuilder();
        builder.append(prefix).append("/").append(alias);
        if (!usage.isEmpty()) {
            builder.append(" ").append(usage);
        }
        builder.append(SPACER_GRAY).append(description);
        return builder.toString();
    }

    public static String fCommand(final String alias, final String usage, final String description) {
        return fCommand(alias, usage, description, C.cWhite);
    }

    public static String fCommand(final String alias, final BaseCommand command) {
        return fCommand(alias, command.getUsage(), command.getDescription());
    }

    public static String fChat(final int level) {
        return C.cGray + level + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fChat(final int level, final PermissionGroup permissionGroup) {
        return C.cGray + level + SPACER + fPermissionGroup(permissionGroup, true, true) + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fPermissionGroup(final PermissionGroup group, final boolean uppercase, final boolean bold) {
        return RESET + group._color + (bold ? C.fBold : "") + (uppercase ? group._prefix.toUpperCase() : group._prefix) + RESET_GRAY;
    }

    public static String fPermissionGroup(final PermissionGroup group, final boolean uppercase) {
        return fPermissionGroup(group, uppercase, false);
    }

    public static String fPermissionGroup(final PermissionGroup group) {
        return fPermissionGroup(group, false, false);
    }

    public static String fInsufficientPermissions() {
        return fMain("Permissions") + "You have insufficient permissions to do this.";
    }

    public static String fBoolean(final boolean toggle) {
        return RESET + (toggle ? C.cGreen : C.cRed) + StringUtils.capitalize(Boolean.toString(toggle)) + RESET_GRAY;
    }

    public static String fSuccess(final String action) {
        return RESET + C.cGreen + action + RESET_GRAY;
    }

    public static String fError(final String action) {
        return RESET + C.cRed + action + RESET_GRAY;
    }

    public static String fError(final String... actions) {
        return RESET + C.cRed + String.join(RESET + C.cRed, actions) + RESET_GRAY;
    }

    public static String fPunishBan(final UUID id, final String reason, final long length) {
        final String formattedLength = length == 0 ? "Permanent" : fTime(length);
        return C.cRed + C.fBold + "You are banned for " + formattedLength + RESET + "\n"
                + reason + RESET + "\n"
                + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.hexuscraft.net" + RESET + "\n\n"
                + C.cDGray + id.toString();
    }

    public static String fCheat(final String name, final String color, final String reason, final int count, final String... server) {
        final StringBuilder builder = new StringBuilder(C.cAqua).append(C.fMagic).append("H")
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

    public static String fCheat(final Player player, final CheatSeverity severity, final String reason, final int count, final String... server) {
        return fCheat(player.getName(), severity.getColor(), reason, count, server);
    }

    private static final double MILLIS_PER_SECOND = 1000;
    private static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    private static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;

    public static String fTime(final long millis, final int trim, final TimeUnit unit) {
        if (millis == -1) {
            return "Permanent";
        }

        final String text;
        final double time;
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
            return text + "s";
        }

        return text;
    }

    public static String fTime(final long millis, final int trim) {
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

    public static String fTime(final long millis) {
        return fTime(millis, 1);
    }

    public static String fTabHeader(final String server) {
        return "\n     " + C.fBold + "Hexuscraft Network" + RESET + "     \n     " + C.cGreen + server + RESET + "     \n";
    }

}
