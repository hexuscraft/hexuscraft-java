package net.hexuscraft.common.enums;

import net.hexuscraft.common.utils.C;

public enum CheatSeverity
{

    LOW(C.cGreen),
    MEDIUM(C.cGold),
    HIGH(C.cRed);

    public String _color;

    CheatSeverity(String color)
    {
        _color = color;
    }
}
