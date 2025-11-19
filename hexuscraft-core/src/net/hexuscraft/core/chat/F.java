package net.hexuscraft.core.chat;

import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.anticheat.CheatSeverity;
import net.hexuscraft.core.command.BaseCommand;
import net.hexuscraft.core.common.UtilMath;
import net.hexuscraft.core.currency.CurrencyType;
import net.hexuscraft.core.permission.IPermission;
import net.hexuscraft.core.permission.PermissionGroup;
import net.hexuscraft.core.punish.PunishData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class F {

    private static final String RESET = C.fReset;
    private static final String RESET_GRAY = C.fReset + C.cGray;

    private static final String SPACER = RESET + " ";
    private static final String SPACER_GRAY = RESET_GRAY + " ";

    private static final String ITEM_COLOR = C.cYellow;

    private static final double MILLIS_PER_SECOND = 1000;
    private static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    private static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;


    public static String fMain(final String prefix, final String... text) {
        return C.cBlue + prefix + ">" + SPACER_GRAY + String.join(RESET_GRAY, text);
    }

    public static String fMain(final MiniPlugin<? extends HexusPlugin> miniPlugin, final String... text) {
        return fMain(miniPlugin._prefix, text);
    }

    public static String fMain(final BaseCommand<? extends MiniPlugin<? extends HexusPlugin>> baseCommand, final String... text) {
        return fMain(baseCommand._miniPlugin, text);
    }

    public static String fMain(final Object object, final String... text) {
        return fMain(object.toString(), text);
    }


    public static String fSub(final String prefix, final String... text) {
        return fMain(C.cDGray + prefix, text);
    }

    public static String fSub(final MiniPlugin<?> miniPlugin, final String... text) {
        return fSub(miniPlugin._prefix, text);
    }

    public static String fSub(final BaseCommand<?> baseCommand, final String... text) {
        return fSub(baseCommand._miniPlugin, text);
    }

    public static String fSub(final Object object, final String... text) {
        return fSub(object.toString(), text);
    }


    public static String fStaff() {
        return C.cAqua + C.fBold + C.fMagic + "#" + SPACER;
    }

    public static String fStaff(final String color) {
        return color + C.fBold + C.fMagic + "#" + SPACER;
    }


    public static String fItem(final String name) {
        return ITEM_COLOR + name;
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

    public static String fItem(final Location location) {
        return fList("" + location.getX(), "" + location.getY(), "" + location.getZ(), fList("" + location.getYaw(), "" + location.getPitch()));
    }


    public static String fList(final String... args) {
        return "[" + fItem(String.join(RESET_GRAY + ", " + fItem(""), args)) + RESET_GRAY + "]";
    }

    public static String fList(final Player... args) {
        return fList(Arrays.stream(args).map(Player::getDisplayName).toArray(String[]::new));
    }

    public static String fList(final int... args) {
        return fList(Arrays.stream(args).mapToObj(Integer::toString).toArray(String[]::new));
    }


    public static String fCurrency(final String color, final String nameSingular, final String namePlural, final int amount) {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural);
    }

    @SuppressWarnings("unused")
    public static String fCurrency(final CurrencyType currencyType, final int amount) {
        return fCurrency(currencyType._color, currencyType._nameSingular, currencyType._namePlural, amount);
    }


    public static String fMatches(final String[] matches, final String searchName) {
        return F.fItem(matches.length + (matches.length == 1 ? " Match" : " Matches")) + RESET_GRAY + " for " + F.fItem(searchName) + RESET_GRAY + (matches.length == 0 ? "." : ": " + F.fList(matches));
    }

    @Deprecated(since = "2025-11-03")
    public static String fMatches(final Player[] matches, final String searchName) {
        return fMatches(Arrays.stream(matches).map(Player::getName).toArray(String[]::new), searchName);
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

    public static String fCommand(final String alias, final BaseCommand<?> command) {
        return fCommand(alias, command.getUsage(), command.getDescription());
    }


    public static String fChat(final int level) {
        return C.cGray + level + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }

    public static String fChat(final int level, final PermissionGroup permissionGroup) {
        return C.cGray + level + SPACER + fPermissionGroup(permissionGroup, true, true) + SPACER + C.cYellow + "%s" + SPACER + "%s";
    }


    public static String fPermissionGroup(final PermissionGroup group, final boolean uppercase, final boolean bold) {
        return group._color + (bold ? C.fBold : "") + (uppercase ? group._prefix.toUpperCase() : group._prefix);
    }

    @SuppressWarnings("unused")
    public static String fPermissionGroup(final PermissionGroup group, final boolean uppercase) {
        return fPermissionGroup(group, uppercase, false);
    }

    public static String fPermissionGroup(final PermissionGroup group) {
        return fPermissionGroup(group, false, false);
    }

    public static String fInsufficientPermissions() {
        return fMain("Permissions", "You have insufficient permissions to do this.");
    }

    public static String fInsufficientPermissions(final IPermission... requiredPermissions) {
        if (requiredPermissions.length > 0)
            return fMain("Permissions", "You have insufficient permissions to do this.\n", F.fSub("", "Required permissions: ", F.fList(Arrays.stream(requiredPermissions).map(IPermission::toString).toArray(String[]::new))));
        return fInsufficientPermissions();
    }

    public static String fInsufficientPermissions(final String... messages) {
        if (messages.length > 0)
            return fMain("Permissions", "You have insufficient permissions to ", F.fItem(F.fList(messages)), ".");
        return fInsufficientPermissions();
    }


    public static String fBoolean(final boolean toggle) {
        return (toggle ? C.cGreen : C.cRed) + StringUtils.capitalize(Boolean.toString(toggle));
    }

    public static String fSuccess(final String... text) {
        return C.cGreen + String.join(RESET + C.cGreen, text);
    }

    public static String fError(final String... text) {
        return C.cRed + String.join(RESET + C.cRed, text);
    }


    public static String fPunish(final PunishData punishData) {
        switch (punishData.type) {
            case WARNING -> {
                return F.fMain("Punish", "You received a warning.\n", F.fMain("", "Reason: ", F.fItem(punishData.reason)), F.fSub("", punishData.uniqueId.toString()));
            }
            case KICK -> {
                return C.cRed + C.fBold + "You were kicked from the server" + RESET + "\n" + punishData.reason + RESET + "\n" + C.cDGreen + "Unfairly removed? Let us know at " + C.cGreen + "www.hexuscraft.net" + RESET + "\n\n" + C.cDGray + punishData.uniqueId.toString();
            }
            case BAN -> {
                return C.cRed + C.fBold + "You are banned for " + F.fTime(punishData.getRemaining()) + RESET + "\n" + punishData.reason + RESET + "\n" + C.cDGreen + "Unfairly banned? Appeal at " + C.cGreen + "www.hexuscraft.net" + RESET + "\n\n" + C.cDGray + punishData.uniqueId.toString();
            }
            case MUTE -> {
                return F.fMain("Punish", "You are muted for ", F.fItem(F.fTime(punishData.length)), "\n", F.fMain("", "Reason: ", F.fItem(punishData.reason)), F.fSub("", punishData.uniqueId.toString()));
            }
        }
        return "<unknown PunishData.type>";
    }


    public static String fCheat(final Player player, final CheatSeverity severity, final String reason) {
        return F.fStaff(severity._color) + F.fMain("AC", F.fItem(player.getDisplayName()), " failed ", severity._color + reason + RESET_GRAY, ".");
    }

    public static String fCheat(final Player player, final CheatSeverity severity, final String reason, final String serverName) {
        return F.fStaff(severity._color) + F.fMain("AC", F.fItem(player.getDisplayName()), " failed ", severity._color + reason + RESET_GRAY, " in ", serverName, ".");
    }


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

    public static String fWelcomeMessage(final String playerName) {
        return String.join(C.fReset + "\n", new String[]{" ", " ", " " + C.cAqua + C.fBold + "Welcome " + playerName + " to Hexuscraft!", " ", " " + C.cGray + "A mini-game server inspired by the legacy Mineplex Network", " " + C.cYellow + "/help" + C.cGray + " for more info", " ", " " + C.cGray + "We are open source! Contribute to help improve our server", " " + C.fUnderline + "https://github.com/hexuscraft", " "});
    }

}
