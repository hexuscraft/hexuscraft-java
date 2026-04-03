package net.hexuscraft.common.utils;

import net.hexuscraft.common.database.data.PunishData;
import net.hexuscraft.common.enums.CheatSeverity;
import net.hexuscraft.common.enums.CurrencyType;
import net.hexuscraft.common.enums.PermissionGroup;

import java.util.concurrent.TimeUnit;

public class F
{

    static final double MILLIS_PER_SECOND = 1000;
    static final double MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    static final double MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;
    static final double MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;


    static String f(String prefix, String... text)
    {
        return prefix + ">" + C.cGray + " " + String.join(C.cGray, text);
    }

    public static String fMain(Object prefix, String... text)
    {
        return f(C.cBlue + prefix.toString(), text);
    }

    public static String fSub(Object prefix, String... text)
    {
        return f(C.cDGray + prefix.toString(), text);
    }

    public static String fStaff(Object prefix, String... text)
    {
        return f(C.cDAqua + prefix.toString(), text);
    }


    public static String fItem(String... args)
    {
        if (args.length == 0)
        {
            return "[]";
        }
        if (args.length == 1)
        {
            return C.cYellow + args[0];
        }
        return C.cGray + "[" + fItem(String.join(C.cGray + ", " + fItem(""), args)) + C.cGray + "]";
    }


    public static String fCurrency(String color, String nameSingular, String namePlural, int amount)
    {
        return color + amount + " " + (amount == 1 ? nameSingular : namePlural);
    }

    public static String fCurrency(CurrencyType currencyType, int amount)
    {
        return fCurrency(currencyType._color, currencyType._nameSingular, currencyType._namePlural, amount);
    }


    public static String fMatches(String[] matches, String searchName)
    {
        return F.fItem(matches.length + (matches.length == 1 ? " Match" : " Matches")) +
                C.cGray +
                " for " +
                F.fItem(searchName) +
                C.cGray +
                (matches.length == 0 ? "." : ": " + F.fItem(matches));
    }


    public static String fCommand(String alias, String usage, String description, String prefix)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append("/").append(alias);
        if (!usage.isEmpty())
        {
            builder.append(" ").append(usage);
        }
        builder.append(C.fReset).append(" ").append(C.cGray).append(description);
        return builder.toString();
    }

    public static String fCommand(String alias, String usage, String description)
    {
        return fCommand(alias, usage, description, C.cWhite);
    }


    public static String fChat(int level)
    {
        return C.cGray + level + C.cYellow + " %s" + C.fReset + " " + "%s";
    }

    public static String fChat(int level, PermissionGroup permissionGroup)
    {
        return C.cGray +
                level +
                " " +
                fPermissionGroup(permissionGroup, true, true) +
                C.cYellow +
                " %s" +
                C.fReset +
                " " +
                "%s";
    }


    public static String fPermissionGroup(PermissionGroup group, boolean uppercase, boolean bold)
    {
        return group._color + (bold ? C.fBold : "") + (uppercase ? group._prefix.toUpperCase() : group._prefix);
    }

    public static String fPermissionGroup(PermissionGroup group)
    {
        return fPermissionGroup(group, false, false);
    }

    public static String fInsufficientPermissions()
    {
        return fMain("Permissions", "You have insufficient permissions to do this.");
    }


    public static String fBoolean(boolean toggle)
    {
        return (toggle ? C.cGreen : C.cRed) + (toggle ? "True" : "False");
    }

    public static String fSuccess(String... text)
    {
        return C.cGreen + String.join(C.cGreen, text);
    }

    public static String fError(String... text)
    {
        return C.cRed + String.join(C.cRed, text);
    }


    public static String fPunish(PunishData punishData)
    {
        switch (punishData._type)
        {
            case WARNING ->
            {
                return F.fMain("Punish", "You received a warning.") +
                        "\n" +
                        F.fMain("", "Reason: ", F.fItem(punishData._reason));
            }
            case KICK ->
            {
                return C.cRed +
                        C.fBold +
                        "You were kicked from the server\n" +
                        C.cWhite +
                        punishData._reason +
                        C.cDGreen +
                        "\nUnfairly removed? Let us know at " +
                        C.cGreen +
                        "www.hexuscraft.net\n\n" +
                        C.cDGray +
                        punishData._uuid.toString();
            }
            case BAN ->
            {
                return C.cRed +
                        C.fBold +
                        "You are banned for " +
                        F.fTime(punishData.getRemaining()) +
                        "\n" +
                        C.cWhite +
                        punishData._reason +
                        C.cDGreen +
                        "\nUnfairly banned? Appeal at " +
                        C.cGreen +
                        "www.hexuscraft.net\n\n" +
                        C.cDGray +
                        punishData._uuid.toString();
            }
            case MUTE ->
            {
                return F.fMain("Punish", "You are muted for ", F.fItem(F.fTime(punishData._length))) +
                        "\n" +
                        F.fMain("", "Reason: ", F.fItem(punishData._reason));
            }
        }
        return "<unknown PunishData.type>";
    }


    public static String fCheat(String playerName, CheatSeverity severity, String reason)
    {
        return F.fStaff(severity._color) +
                F.fMain("AC", F.fItem(playerName), " failed ", severity._color + reason + C.cGray, ".");
    }

    public static String fCheat(String playerName, CheatSeverity severity, String reason, String serverName)
    {
        return F.fStaff(severity._color) +
                F.fMain("AC",
                        F.fItem(playerName),
                        " failed ",
                        severity._color + reason + C.cGray,
                        " in ",
                        serverName,
                        ".");
    }


    public static String fTime(long millis, int trim, TimeUnit unit)
    {
        if (millis == -1)
        {
            return "Permanent";
        }

        String text;
        double time;
        if (unit == TimeUnit.DAYS)
        {
            text = (time = UtilMath.trim(millis / MILLIS_PER_DAY, trim)) + " Day";
        }
        else if (unit == TimeUnit.HOURS)
        {
            text = (time = UtilMath.trim(millis / MILLIS_PER_HOUR, trim)) + " Hour";
        }
        else if (unit == TimeUnit.MINUTES)
        {
            text = (time = UtilMath.trim(millis / MILLIS_PER_MINUTE, trim)) + " Minute";
        }
        else if (unit == TimeUnit.SECONDS)
        {
            text = (time = UtilMath.trim(millis / MILLIS_PER_SECOND, trim)) + " Second";
        }
        else
        {
            text = (time = UtilMath.trim(millis)) + " Millisecond";
        }

        if (time != 1)
        {
            return text + "s";
        }

        return text;
    }

    public static String fTime(long millis, int trim)
    {
        if (millis < MILLIS_PER_MINUTE)
        {
            return fTime(millis, trim, TimeUnit.SECONDS);
        }
        if (millis < MILLIS_PER_HOUR)
        {
            return fTime(millis, trim, TimeUnit.MINUTES);
        }
        if (millis < MILLIS_PER_DAY)
        {
            return fTime(millis, trim, TimeUnit.HOURS);
        }
        return fTime(millis, trim, TimeUnit.DAYS);
    }

    public static String fTime(long millis)
    {
        return fTime(millis, 1);
    }

    public static String fTabHeader(String server)
    {
        return "\n     " + C.fBold + "Hexuscraft Network" + C.fReset + "     \n     " + C.cGreen + server + "     \n";
    }

    public static String fWelcomeMessage(String playerName)
    {
        return String.join("§r\n",
                " ",
                " §b§lWelcome " + playerName + " to Hexuscraft!",
                "  §7A mini-game server inspired by the legacy Mineplex Network",
                "   §7Type §e/help§7 to see available commands ",
                " ",
                "  §7We are open source! Contribute to help improve our server",
                "   §e§nhttps://github.com/hexuscraft",
                " ");
    }

}
