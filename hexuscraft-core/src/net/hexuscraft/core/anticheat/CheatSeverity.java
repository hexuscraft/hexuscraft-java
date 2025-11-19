package net.hexuscraft.core.anticheat;

import net.hexuscraft.core.chat.C;

public enum CheatSeverity {

    LOW(C.cGreen), MEDIUM(C.cGold), HIGH(C.cRed);

    CheatSeverity(final String color) {
        _color = color;
    }

    public final String _color;
}
