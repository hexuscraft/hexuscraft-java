package net.hexuscraft.common.utils;

import net.hexuscraft.common.IPermission;
import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.enums.CheatSeverity;
import net.hexuscraft.common.enums.CurrencyType;
import net.hexuscraft.common.enums.PermissionGroup;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class F {

    private static final double MILLIS_PER_SECOND = 1000;
    private static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    private static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    private static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;


    public static String fMain(final String prefix, final String... text) {
        return C.cBlue + prefix + ">" + C.cGray + " " + String.join(C.cGray, text);
    }

    public static String fMain(final Object object, final String... text) {
        return fMain(object.toString(), text);
    }


    public static String fSub(final String prefix, final String... text) {
        return fMain(C.cDGray + prefix, text);
    }

    public static String fSub(final Object object, final String... text) {
        return fSub(object.toString(), text);
    }


    public static String fStaff() {
        return C.cAqua + C.fBold + C.fMagic + "#" + C.fReset + " ";
    }

    public static String fStaff(final String color) {
        return color + C.fBold + C.fMagic + "#" + C.fReset + " ";
    }


    public static String fItem(final AtomicReference<?> atomicReference) {
        return fItem(String.valueOf(atomicReference.get()));
    }

    public static String fItem(final String name) {
        return C.cYellow + name;
    }

    public static String fItem(final String... args) {
        if (args.length == 0) return "[]";
        if (args.length == 1) return C.cYellow + args[0];
        return C.cGray + "[" + fItem(String.join(C.cGray + ", " + fItem(""), args)) + C.cGray + "]";
    }

    public static String fItem(final String name, final int count) {
        return fItem(count + " " + name);
    }

    public static String fItem(final int... args) {
        return fItem(Arrays.stream(args).mapToObj(Integer::toString).toArray(String[]::new));
    }


    public static String fCurrency(final String color, final String nameSingular, final String namePlural,
                                   final int amount) {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural);
    }

    public static String fCurrency(final CurrencyType currencyType, final int amount) {
        return fCurrency(currencyType._color, currencyType._nameSingular, currencyType._namePlural, amount);
    }


    public static String fMatches(final String[] matches, final String searchName) {
        return F.fItem(matches.length + (matches.length == 1 ? " Match" : " Matches")) + C.cGray + " for " +
                F.fItem(searchName) + C.cGray + (matches.length == 0 ? "." : ": " + F.fItem(matches));
    }


    public static String fCommand(final String alias, final String usage, final String description,
                                  final String prefix) {
        final StringBuilder builder = new StringBuilder();
        builder.append(prefix).append("/").append(alias);
        if (!usage.isEmpty()) {
            builder.append(" ").append(usage);
        }
        builder.append(C.fReset).append(" ").append(C.cGray).append(description);
        return builder.toString();
    }

    public static String fCommand(final String alias, final String usage, final String description) {
        return fCommand(alias, usage, description, C.cWhite);
    }


    public static String fChat(final int level) {
        return C.cGray + level + C.cYellow + " %s" + C.fReset + " " + "%s";
    }

    public static String fChat(final int level, final PermissionGroup permissionGroup) {
        return C.cGray + level + " " + fPermissionGroup(permissionGroup, true, true) + C.cYellow + " %s" + C.fReset +
                " " + "%s";
    }


    public static String fStaffChat(final String senderServerName, final PermissionGroup senderPrimaryGroup,
                                    final String senderName, final String message) {
        return C.cPurple + senderServerName + " " + fPermissionGroup(senderPrimaryGroup) + " " + senderName +
                C.cPurple + " " + message;
    }

    public static String fStaffChat(final String senderServerName, final PermissionGroup senderPrimaryGroup,
                                    final String senderName, final String targetServerName,
                                    final PermissionGroup targetPrimaryGroup, final String targetName,
                                    final String message) {
        return C.cPurple + senderServerName + " " + fPermissionGroup(senderPrimaryGroup) + " " + senderName +
                C.cPurple + " -> " + targetServerName + " " + fPermissionGroup(targetPrimaryGroup) + " " + targetName +
                C.cPurple + " " + message;
    }


    public static String fPermissionGroup(final PermissionGroup group, final boolean uppercase, final boolean bold) {
        return group._color + (bold ? C.fBold : "") + (uppercase ? group._prefix.toUpperCase() : group._prefix);
    }

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
            return fMain("Permissions", "You have insufficient permissions to do this.\n",
                    F.fSub("", "Required permissions: ",
                            F.fItem(Arrays.stream(requiredPermissions).map(IPermission::toString)
                                    .toArray(String[]::new))));
        return fInsufficientPermissions();
    }

    public static String fInsufficientPermissions(final String... messages) {
        if (messages.length > 0)
            return fMain("Permissions", "You have insufficient permissions to ", F.fItem(F.fItem(messages)), ".");
        return fInsufficientPermissions();
    }


    public static String fBoolean(final boolean toggle) {
        return (toggle ? C.cGreen : C.cRed) + (toggle ? "True" : "False");
    }

    public static String fSuccess(final String... text) {
        return C.cGreen + String.join(C.cGreen, text);
    }

    public static String fError(final String... text) {
        return C.cRed + String.join(C.cRed, text);
    }


    public static String fPunish(final PunishData punishData) {
        switch (punishData.type) {
            case WARNING -> {
                return F.fMain("Punish", "You received a warning.") + "\n" +
                        F.fMain("", "Reason: ", F.fItem(punishData.reason));
            }
            case KICK -> {
                return C.cRed + C.fBold + "You were kicked from the server\n" + C.cWhite + punishData.reason +
                        C.cDGreen + "\nUnfairly removed? Let us know at " + C.cGreen + "www.hexuscraft.net\n\n" +
                        C.cDGray + punishData.uniqueId.toString();
            }
            case BAN -> {
                return C.cRed + C.fBold + "You are banned for " + F.fTime(punishData.getRemaining()) + "\n" + C.cWhite +
                        punishData.reason + C.cDGreen + "\nUnfairly banned? Appeal at " + C.cGreen +
                        "www.hexuscraft.net\n\n" + C.cDGray + punishData.uniqueId.toString();
            }
            case MUTE -> {
                return F.fMain("Punish", "You are muted for ", F.fItem(F.fTime(punishData.length))) + "\n" +
                        F.fMain("", "Reason: ", F.fItem(punishData.reason));
            }
        }
        return "<unknown PunishData.type>";
    }


    public static String fCheat(final String playerName, final CheatSeverity severity, final String reason) {
        return F.fStaff(severity._color) +
                F.fMain("AC", F.fItem(playerName), " failed ", severity._color + reason + C.cGray, ".");
    }

    public static String fCheat(final String playerName, final CheatSeverity severity, final String reason,
                                final String serverName) {
        return F.fStaff(severity._color) +
                F.fMain("AC", F.fItem(playerName), " failed ", severity._color + reason + C.cGray, " in ", serverName,
                        ".");
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
        return "\n     " + C.fBold + "Hexuscraft Network" + C.fReset + "     \n     " + C.cGreen + server + "     \n";
    }

    public static String fWelcomeMessage(final String playerName) {
        return String.join("§r\n", " ", " §b§lWelcome " + playerName + " to Hexuscraft!",
                "  §7A mini-game server inspired by the legacy Mineplex Network",
                "   §7Type §e/help§7 to see available commands ", " ",
                "  §7We are open source! Contribute to help improve our server", "   §e§nhttps://github.com/hexuscraft",
                " ");
    }

}
