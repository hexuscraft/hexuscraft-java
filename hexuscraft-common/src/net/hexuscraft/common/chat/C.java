package net.hexuscraft.common.chat;

import java.util.Map;

@SuppressWarnings("unused")
public final class C {

    public static final String fBold = "§l";
    public static final String fItalic = "§o";
    public static final String fMagic = "§k";
    public static final String fReset = "§r";
    public static final String fStrikethrough = "§m";
    public static final String fUnderline = "§n";

    public static final String cAqua = "§b";
    public static final String cBlack = "§0";
    public static final String cBlue = "§9";
    public static final String cDAqua = "§3";
    public static final String cDBlue = "§1";
    public static final String cDGray = "§8";
    public static final String cDGreen = "§2";
    public static final String cDPurple = "§5";
    public static final String cDRed = "§4";
    public static final String cGold = "§6";
    public static final String cGray = "§7";
    public static final String cGreen = "§a";
    public static final String cPurple = "§d";
    public static final String cRed = "§c";
    public static final String cWhite = "§f";
    public static final String cYellow = "§e";

    public static final Map<Integer, String> hexMap =
            Map.ofEntries(Map.entry(0, cBlack), Map.entry(1, cBlue), Map.entry(2, cDGreen), Map.entry(3, cDAqua),
                    Map.entry(4, cDRed), Map.entry(5, cDPurple), Map.entry(6, cGold), Map.entry(7, cGray),
                    Map.entry(8, cDGray), Map.entry(9, cBlue), Map.entry(10, cGreen), Map.entry(11, cAqua),
                    Map.entry(12, cRed), Map.entry(13, cPurple), Map.entry(14, cYellow), Map.entry(15, cWhite));

}