package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.chat.C;

public enum CheatSeverity {

    LOW(C.cGreen),
    MEDIUM(C.cGold),
    HIGH(C.cRed)

    ;

    CheatSeverity(String color) {
        this.color = color;
    }

    private final String color;

    public String getColor() {
        return color;
    }
}
