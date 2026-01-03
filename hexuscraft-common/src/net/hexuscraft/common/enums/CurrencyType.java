package net.hexuscraft.common.enums;

import net.hexuscraft.common.chat.C;

@SuppressWarnings("unused")
public enum CurrencyType {

    COIN("Coin", "Coins", C.cGold),
    XP("XP", "XP", C.cGold);

    public final String _nameSingular;
    public final String _namePlural;
    public final String _color;

    CurrencyType(final String nameSingular, final String namePlural, final String color) {
        _nameSingular = nameSingular;
        _namePlural = namePlural;
        _color = color;
    }

}
