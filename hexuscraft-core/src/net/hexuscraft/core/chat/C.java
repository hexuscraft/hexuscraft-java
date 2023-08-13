package net.hexuscraft.core.chat;

import org.bukkit.ChatColor;

public class C {

//	public static final String fBold = ChatColor.COLOR_CHAR + "l";
//	public static final String fItalic = ChatColor.COLOR_CHAR + "o";
//	public static final String fMagic = ChatColor.COLOR_CHAR + "k";
//	public static final String fReset = ChatColor.COLOR_CHAR + "r";
//	public static final String fStrikethrough = ChatColor.COLOR_CHAR + "m";
//	public static final String fUnderline = ChatColor.COLOR_CHAR + "n";
//
//	public static final String cAqua = ChatColor.COLOR_CHAR + "b";
//	public static final String cBlack = ChatColor.COLOR_CHAR + "0";
//	public static final String cBlue = ChatColor.COLOR_CHAR + "9";
//	public static final String cDAqua = ChatColor.COLOR_CHAR + "3";
//	public static final String cDBlue = ChatColor.COLOR_CHAR + "1";
//	public static final String cDGray = ChatColor.COLOR_CHAR + "8";
//	public static final String cDGreen = ChatColor.COLOR_CHAR + "2";
//	public static final String cDPurple = ChatColor.COLOR_CHAR + "5";
//	public static final String cDRed = ChatColor.COLOR_CHAR + "4";
//	public static final String cGold = ChatColor.COLOR_CHAR + "6";
//	public static final String cGray = ChatColor.COLOR_CHAR + "7";
//	public static final String cGreen = ChatColor.COLOR_CHAR + "a";
//	public static final String cPurple = ChatColor.COLOR_CHAR + "d";
//	public static final String cRed = ChatColor.COLOR_CHAR + "c";
//	public static final String cWhite = ChatColor.COLOR_CHAR + "f";
//	public static final String cYellow = ChatColor.COLOR_CHAR + "e";

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