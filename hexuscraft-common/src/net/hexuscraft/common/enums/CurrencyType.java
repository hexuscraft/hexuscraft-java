package net.hexuscraft.common.enums;

import net.hexuscraft.common.utils.C;

public enum CurrencyType
{

    COIN("Coin", "Coins", C.cGold),
    XP("XP", "XP", C.cGold);

    public String _nameSingular;
    public String _namePlural;
    public String _color;

    CurrencyType(String nameSingular, String namePlural, String color)
    {
        _nameSingular = nameSingular;
        _namePlural = namePlural;
        _color = color;
    }

}
