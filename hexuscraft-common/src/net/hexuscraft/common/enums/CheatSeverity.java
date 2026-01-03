package net.hexuscraft.common.enums;

import net.hexuscraft.common.chat.C;

public enum CheatSeverity {

    LOW(C.cGreen),
    MEDIUM(C.cGold),
    HIGH(C.cRed);

    public final String _color;

    CheatSeverity(final String color) {
        _color = color;
    }
}
