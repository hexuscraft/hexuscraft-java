package net.hexuscraft.core.chat;

import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public final class C {

    public static final String fBold = ChatColor.BOLD.toString();
    public static final String fItalic = ChatColor.ITALIC.toString();
    public static final String fMagic = ChatColor.MAGIC.toString();
    public static final String fReset = ChatColor.RESET.toString();
    public static final String fStrikethrough = ChatColor.STRIKETHROUGH.toString();
    public static final String fUnderline = ChatColor.UNDERLINE.toString();

    public static final String cAqua = ChatColor.AQUA.toString();
    public static final String cBlack = ChatColor.BLACK.toString();
    public static final String cBlue = ChatColor.BLUE.toString();
    public static final String cDAqua = ChatColor.DARK_AQUA.toString();
    public static final String cDBlue = ChatColor.DARK_BLUE.toString();
    public static final String cDGray = ChatColor.DARK_GRAY.toString();
    public static final String cDGreen = ChatColor.DARK_GREEN.toString();
    public static final String cDPurple = ChatColor.DARK_PURPLE.toString();
    public static final String cDRed = ChatColor.DARK_RED.toString();
    public static final String cGold = ChatColor.GOLD.toString();
    public static final String cGray = ChatColor.GRAY.toString();
    public static final String cGreen = ChatColor.GREEN.toString();
    public static final String cPurple = ChatColor.LIGHT_PURPLE.toString();
    public static final String cRed = ChatColor.RED.toString();
    public static final String cWhite = ChatColor.WHITE.toString();
    public static final String cYellow = ChatColor.YELLOW.toString();

    public static ChatColor colorFromC(String c) {
        return ChatColor.getByChar(c.charAt(1));
    }

    public static net.md_5.bungee.api.ChatColor bungeeColorFromC(String c) {
        return colorFromC(c).asBungee();
    }

}