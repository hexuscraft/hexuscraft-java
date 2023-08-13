package net.hexuscraft.core.currency;

import net.hexuscraft.core.chat.C;

public enum CurrencyType {

    COIN("Coin", "Coins", C.cGold);

    private final String _nameSingular;
    private final String _namePlural;
    private final String _color;
    
    CurrencyType(String nameSingular, String namePlural, String color) {
        _nameSingular = nameSingular;
        _namePlural = namePlural;
        _color = color;
    }

    public String getColor() {
        return _color;
    }

    public String getNameSingular() {
        return _nameSingular;
    }

    public String getNamePlural() {
        return _namePlural;
    }


}
