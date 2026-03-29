package net.hexuscraft.common.utils;

import java.util.Map;

public class C
{

    public static String fBold = "§l";
    public static String fItalic = "§o";
    public static String fMagic = "§k";
    public static String fReset = "§r";
    public static String fStrikethrough = "§m";
    public static String fUnderline = "§n";

    public static String cAqua = "§b";
    public static String cBlack = "§0";
    public static String cBlue = "§9";
    public static String cDAqua = "§3";
    public static String cDBlue = "§1";
    public static String cDGray = "§8";
    public static String cDGreen = "§2";
    public static String cDPurple = "§5";
    public static String cDRed = "§4";
    public static String cGold = "§6";
    public static String cGray = "§7";
    public static String cGreen = "§a";
    public static String cPurple = "§d";
    public static String cRed = "§c";
    public static String cWhite = "§f";
    public static String cYellow = "§e";

    public static Map<Integer, String> hexMap = Map.ofEntries(Map.entry(0, cBlack),
            Map.entry(1, cBlue),
            Map.entry(2, cDGreen),
            Map.entry(3, cDAqua),
            Map.entry(4, cDRed),
            Map.entry(5, cDPurple),
            Map.entry(6, cGold),
            Map.entry(7, cGray),
            Map.entry(8, cDGray),
            Map.entry(9, cBlue),
            Map.entry(10, cGreen),
            Map.entry(11, cAqua),
            Map.entry(12, cRed),
            Map.entry(13, cPurple),
            Map.entry(14, cYellow),
            Map.entry(15, cWhite));

}